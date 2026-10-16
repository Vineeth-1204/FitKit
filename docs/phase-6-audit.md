# FitKit Phase 6 — Audit: Nutrition Data Quality & Custom Recipe Management

## 1. Executive Summary

A comprehensive, read-only audit of the FitKit repository and database was performed to evaluate the existing food catalog, nutrition models, recipe calculation engine, provenance records, and authorization architecture in preparation for Phase 6.

FitKit currently possesses:
- A verified catalog of 26 canonical foods and dishes with proximate nutritional profiles, moisture, and ash content.
- Deterministic simulation of recipe hydration, moisture loss, and yield scaling for 3 predefined recipes (`REC_CHAP_01`, `REC_IDLI_01`, `REC_DAL_01`).
- Persistent meal logging with immutable historical nutrient snapshots (`MealEntry`).
- Strict server-side authentication and account isolation via Spring Security 6 session cookies (implemented in Phase 5).

However, **the existing recipe system does NOT support user-created or user-owned recipes**. Recipes are currently global, static system records without a `user_id` or ownership model. The in-memory calculator (`POST /api/recipes/custom-calculate`) does not persist custom formulations to the database, cannot be saved, edited, or listed by user, and cannot be selected from the meal logging modal.

Furthermore, an audit of the food catalog reveals minor inconsistencies in energy conversion factors, missing micronutrient datasets, and opportunities to add key staple Indian ingredients with full provenance (such as Paneer Bhurji / Upma / Pulao ingredients like onion, tomato, cumin, turmeric, green chili, and mustard oil).

---

## 2. Existing Capabilities Verified in Code

### 2.1 Domain & Entities
- **`FoodItem`**: Tracks `foodCode`, `source` (`SourceProvenance`), `foodState` (`RAW_INGREDIENT`, `COOKED_SINGLE`, `PREPARED_RECIPE`, `PACKAGED_COMMERCIAL`), `foodGroup`, `name`, `brandName`, and verification status.
- **`FoodNutrient`**: Stores `energyKcal`, `energyKj`, `proteinG`, `availableCarbohydratesG`, `totalFatG`, `dietaryFiberG`, `moistureWaterG`, `ashG`, `qualityStatus`, `correctionNotes`, and `micronutrientsJson`. Missing nutrient fields are nullable `BigDecimal` rather than coerced zeros.
- **`FoodPortion`**: Provides household measures with gram weights (e.g. 1 medium katori = 150g, 1 cup cooked = 160g, 1 standard idli = 50g).
- **`Recipe`**: Stores `recipeCode`, `title`, `totalRawWeightG`, `cookingYieldFactor`, `cookedWeightG`, `servingsYield`, `servingWeightG`, `cookingMethod`, `preparationNotes`, and `uncertaintyRating`. Currently lacks `userId`, `isCustom`, and `isActive` attributes.
- **`RecipeIngredient`**: Links `Recipe` to `FoodItem` with `rawQuantityG`, `unitMeasure`, and optional `retentionFactorOverride`.
- **`MealEntry`**: Captures immutable nutrient snapshots (`energyKcal`, `proteinG`, `carbohydratesG`, `fatG`, `fiberG`) at the time of logging. References `recipe_id` with `ON DELETE SET NULL`.

### 2.2 Services & Calculation Engine
- **`RecipeCalculationService`**:
  - Aggregates raw ingredient weights and nutrients per 100g.
  - Multiplies raw batch weight by `cookingYieldFactor` ($Y = \text{Cooked Weight} / \text{Raw Weight}$).
  - Computes final cooked batch weight and divides by `servingsYield` to yield serving weight.
  - Correctly calculates per-100g density by dividing batch totals by cooked batch weight, and per-serving amounts by dividing batch totals by `servingsYield`.
- **`MealLoggingService`**:
  - Resolves food or recipe, computes nutrient snapshots using `RecipeCalculationService`, and stores the immutable historical record in `MealEntry`.
  - Enforces strict ownership checks on updates and deletions (`entry.getMealLog().getUserId().equalsIgnoreCase(authenticatedUserId)`).

### 2.3 Security & API Context
- Spring Security 6 session-based authentication (`JSESSIONID`, `X-XSRF-TOKEN`).
- User identity is authoritative via `Principal.getName()`.
- Public routes: `/api/foods/**`, `/api/recipes/**`, `/api/stats/**`.
- Protected routes: `/api/meals/**`, `/api/targets/**`. Custom recipe management must be placed under authenticated routes.

---

## 3. Missing Recipe-Management Functionality

1. **No User Ownership on Recipes**: The `recipes` table has no `user_id` or `is_custom` column. All recipes are currently global.
2. **No Persistence for Custom Recipes**: While `POST /api/recipes/custom-calculate` computes a calculation preview in memory, it does not persist the recipe, its ingredients, or its yield parameters to the database.
3. **No CRUD Endpoints for Custom Recipes**:
   - No `POST /api/custom-recipes` to save a custom recipe.
   - No `GET /api/custom-recipes` to list recipes created by the authenticated user.
   - No `GET /api/custom-recipes/{id}` to fetch an owned recipe with its ingredients.
   - No `PUT /api/custom-recipes/{id}` to update an owned recipe.
   - No `DELETE /api/custom-recipes/{id}` to safely delete or archive an owned recipe.
4. **No Custom Recipe Logging Integration**: The frontend Add Food modal only searches catalog foods; users cannot select their saved custom recipes to log into Breakfast, Lunch, Dinner, or Snacks.
5. **No Recipe Builder Frontend**: The Recipe Lab view only contains sliders for preset recipes. It lacks a dynamic UI to add ingredients, search catalog foods, adjust gram quantities, set batch yield / cooked weight, define servings, preview nutrition, and save the custom recipe.

---

## 4. Current Nutrition-Data Quality Limitations

1. **Catalog Gaps for Common Recipe Ingredients**: To construct authentic Indian home recipes (e.g., Vegetable Upma, Paneer Bhurji, Vegetable Pulao, Egg Dosa, Homemade Dal Tadka), several fundamental raw ingredients and spices are absent:
   - Raw Onion (`Allium cepa`)
   - Raw Tomato (`Solanum lycopersicum`)
   - Fresh Green Chili (`Capsicum annuum`)
   - Whole Cumin Seeds / Jeera (`Cuminum cyminum`)
   - Turmeric Powder (`Curcuma longa`)
   - Raw Mustard Oil (`Brassica juncea`)
   - Semolina / Suji / Rava (`Triticum durum`) for Upma
2. **Missing Value Differentiation**: Nutrient entries in `food_nutrients` must preserve genuine zeros (e.g. 0.0g carbs in pure ghee or vegetable oil) as distinct from unanalyzed / unknown micronutrients (which must remain `NULL`).
3. **Yield Factor vs Final Cooked Batch Weight**: If a user directly weighs their final cooked pot (e.g. 650g), the system must compute the effective yield factor ($Y = 650 / W_{\text{raw}}$) rather than asking the user to guess a yield factor or accidentally applying a yield multiplier twice.
4. **Energy Unit Consistency**: All kcal and kJ values must maintain strict Atwater factors ($4 \cdot P + 4 \cdot C + 9 \cdot F + 2 \cdot \text{Fiber}$) and $1 \text{ kcal} = 4.184 \text{ kJ}$.

---

## 5. Proposed Schema Changes (Flyway Migration `V6`)

### 5.1 Update `recipes` Table
Add the following columns to `recipes`:
```sql
ALTER TABLE recipes
    ADD COLUMN user_id VARCHAR(191) NULL AFTER food_id,
    ADD COLUMN is_custom BOOLEAN NOT NULL DEFAULT FALSE AFTER user_id,
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER is_custom,
    ADD COLUMN description VARCHAR(500) NULL AFTER title;

CREATE INDEX idx_recipes_user_id ON recipes(user_id);
CREATE INDEX idx_recipes_is_custom ON recipes(is_custom);
```
- For existing system recipes (`REC_CHAP_01`, `REC_IDLI_01`, `REC_DAL_01`), `user_id` is `NULL`, `is_custom = FALSE`, `is_active = TRUE`.
- For user-created recipes, `user_id` is set to the authenticated user's email (`Principal.getName()`), `is_custom = TRUE`, `is_active = TRUE`.
- Soft deletion is supported via `is_active = FALSE`. If a user deletes a custom recipe, previous `meal_entries` referencing `recipe_id` remain intact without foreign key violations.

### 5.2 Expand Indian Food Ingredients in Seed Migration `V6`
Seed authentic IFCT 2017 & USDA FDC ingredient profiles for recipe formulation:
- Semolina / Suji / Rava (IFCT A018)
- Onion, fresh raw (IFCT C012)
- Tomato, ripe raw (IFCT C035)
- Green Chili (IFCT D005)
- Cumin Seeds / Jeera (IFCT D004)
- Turmeric powder (IFCT D028)
- Mustard oil, raw cold-pressed (IFCT T017)

All seeded records will retain explicit `source_id`, `source_record_id`, `quality_status`, and verified household portion measures.

---

## 6. Authorization & Data Isolation Requirements

1. **Authentication Enforcement**:
   - `POST /api/custom-recipes`: Authenticated users only.
   - `GET /api/custom-recipes`: Authenticated users only; returns only recipes where `user_id = :principal`.
   - `GET /api/custom-recipes/{id}`: Returns 200 OK only if `recipe.user_id = :principal`. If owned by another user, returns 403 Forbidden or 404 Not Found.
   - `PUT /api/custom-recipes/{id}`: Verifies `recipe.user_id = :principal`. Rejects unauthorized mutations.
   - `DELETE /api/custom-recipes/{id}`: Verifies `recipe.user_id = :principal`. Soft-deletes (`is_active = false`).
2. **Public Catalog Isolation**:
   - `GET /api/recipes` continues to return only canonical shared recipes (`is_custom = false` and `user_id IS NULL`). User-created recipes are never exposed in public catalog searches.
3. **Meal Logging Isolation**:
   - When logging via `POST /api/meals/entries` with a `recipeId`, verify that the recipe is either a public canonical recipe (`is_custom = false`) or owned by the authenticated user (`recipe.user_id = :principal`). Users cannot log another user's private custom recipe.

---

## 7. Risks to Existing Meal Logs and Recipes

| Risk | Mitigation |
|---|---|
| Modifying a recipe alters past logged meals | `MealEntry` already stores an immutable historical snapshot (`energyKcal`, `proteinG`, `carbohydratesG`, `fatG`, `fiberG`). Changing a recipe only affects future logs. |
| Deleting a custom recipe causes foreign key violation in `meal_entries` | Database already has `ON DELETE SET NULL` on `meal_entries.recipe_id`. In addition, recipes will use soft-deletion (`is_active = false`), preserving referential integrity. |
| User supplies final cooked weight and yield factor, leading to double-yield error | If final cooked weight is supplied, calculate yield factor as $W_{\text{cooked}} / W_{\text{raw}}$ and do not multiply by yield factor again. |
| Incomplete ingredient nutrients distort recipe totals | Clearly flag missing values in the DTO (`calculationNotes`, `hasIncompleteNutrients`). |

---

## 8. Prioritized Implementation Steps

1. **Phase A**: Complete this read-only audit deliverable (`docs/phase-6-audit.md`).
2. **Phase B**: Document catalog quality audit and data corrections (`docs/phase-6-nutrition-data-quality.md`).
3. **Phase C & D**: Database migration `V6` (add `user_id`, `is_custom`, `is_active` to `recipes`, seed staple recipe ingredients).
4. **Phase E**: Extend `RecipeCalculationService` to handle user-specified cooked batch weights, servings, and missing nutrient tracking.
5. **Phase F**: Build `CustomRecipeService` and `CustomRecipeController` with full CRUD, validation, and ownership enforcement.
6. **Phase G**: Update `MealLoggingService` to allow logging owned custom recipes into meal diaries with immutable snapshot creation.
7. **Phase H**: Extend frontend UI in Recipe Lab (`index.html`, `styles.css`, `app.js`) with custom recipe creator, ingredient selector, and diary logging integration.
8. **Phase I**: Security and configuration hygiene (address credential in `application.properties`, configure profile-aware demo seeding).
9. **Phase J**: Automated testing (JUnit 5, MockMvc, User A vs User B isolation, yield precision, regression suite).
10. **Phase K**: MySQL 8.0 live verification, browser recording, and final report (`docs/phase-6-implementation-report.md`).
