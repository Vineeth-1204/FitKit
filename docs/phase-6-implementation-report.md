# FitKit — Phase 6: Nutrition Data Quality & Custom Recipe Management Implementation Report

**Project:** FitKit Nutrition Core  
**Phase:** Phase 6 — Nutrition Data Quality & User-Owned Custom Recipe Management  
**Date:** October 15, 2026  
**Status:** Complete & Fully Verified  

---

## 1. Features Verified Before Implementation

Prior to modifying code, a comprehensive audit was executed across the existing schema, entities, services, APIs, and client interface (documented in [`docs/phase-6-audit.md`](file:///d:/Projects/WebProgramming/docs/phase-6-audit.md)):
- **Session Authentication & Authorization:** Spring Security 6 session-based authentication was active with user-specific isolation for daily meal logs (`/api/meals/**`) and user targets (`/api/targets/**`).
- **Catalog Structure:** 26 catalog foods were stored with proximate nutrients (`PER_100G` basis).
- **Predefined Recipes:** 3 canonical recipes (`REC_CHAP_01`, `REC_IDLI_01`, `REC_DAL_01`) existed in the catalog, but the recipe engine did not support user-created recipes, user isolation, or recipe formulation mutations.
- **Yield Handling:** `RecipeCalculationService` supported cooking yield multipliers, but did not handle user-specified authoritative final cooked weights or prevent double-application of yield factors.
- **Portion Selection:** Household measures (katori, piece, bowl) were mapped to defensible gram conversions.

---

## 2. Nutrition-Data Issues Found & Corrected

A full catalog audit was conducted across all 26 existing items and documented in [`docs/phase-6-nutrition-data-quality.md`](file:///d:/Projects/WebProgramming/docs/phase-6-nutrition-data-quality.md):
1. **Raw vs. Cooked State Classification:**
   - Raw grains/pulses (`RAW_INGREDIENT`) and boiled/cooked grains (`COOKED_SINGLE`) are explicitly classified.
   - White Rice Raw (`USDA_168878`, 365 kcal/100g) and Cooked Rice (`USDA_2345388`, 130 kcal/100g) remain distinct; moisture uptake (~2.8x) is modeled deterministically.
2. **Distinguishing Genuine Zeros from Missing/Unknown Values:**
   - Fats and oils (Mustard oil, Ghee, Butter) contain verified genuine 0.0g carbohydrates, 0.0g protein, and 0.0g fiber.
   - Missing micronutrients or unanalyzed values are preserved as SQL `NULL` rather than coerced to `0.0`.
   - `RecipeCalculationResultDto` and calculation engine expose `hasIncompleteNutrients` boolean flag to inform users when any formulation component lacks full proximate data.
3. **Energy Unit Conversions & Atwater Validation:**
   - Evaluated 4-4-9 Atwater general factor energy against specific IFCT energy values.
   - Retained canonical $1\text{ kcal} = 4.184\text{ kJ}$ conversions.

---

## 3. Data Sources & Provenance for Additions

To support everyday Indian custom recipes (e.g., Vegetable Upma, Egg Dosa, Paneer Bhurji, Dal Tadka, Vegetable Pulao), staple raw ingredients introduced in migration `V6` were reconciled in migration `V7` with authoritative **Indian Food Composition Tables (IFCT 2017)**, National Institute of Nutrition (ICMR):

| Food Code | Food Item Name | State | Food Group | Energy (kcal) | Protein (g) | Carbs (g) | Fat (g) | Source & License |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `IFCT_A022` | Semolina / Suji / Rava | `RAW_INGREDIENT` | Cereals and Millets | 333.7 | 11.38 | 68.43 | 0.80 | IFCT 2017 A022 (ICMR-NIN) Open Gov Data |
| `IFCT_G017` | Onion, fresh raw | `RAW_INGREDIENT` | Other Vegetables | 48.5 | 1.20 | 9.30 | 0.40 | IFCT 2017 G017 (ICMR-NIN) Open Gov Data |
| `IFCT_D075` | Tomato, ripe raw | `RAW_INGREDIENT` | Other Vegetables | 20.8 | 0.90 | 3.60 | 0.20 | IFCT 2017 D075 (ICMR-NIN) Open Gov Data |
| `IFCT_G011` | Green chili, raw | `RAW_INGREDIENT` | Condiments and Spices | 45.7 | 2.60 | 6.80 | 0.60 | IFCT 2017 G011 (ICMR-NIN) Open Gov Data |
| `IFCT_G025` | Cumin seeds (Jeera) | `RAW_INGREDIENT` | Condiments and Spices | 304.5 | 18.70 | 36.60 | 15.00 | IFCT 2017 G025 (ICMR-NIN) Open Gov Data |
| `IFCT_G026` | Turmeric powder (Haldi) | `RAW_INGREDIENT` | Condiments and Spices | 280.6 | 6.30 | 64.90 | 3.20 | IFCT 2017 G026 (ICMR-NIN) Open Gov Data |
| `IFCT_T017` | Mustard oil, cold-pressed | `RAW_INGREDIENT` | Fats and Oils | 884.0 | 0.00 | 0.00 | 100.00 | IFCT 2017 T017 (ICMR-NIN) Open Gov Data |

---

## 4. Recipe Calculation Methodology

The calculation engine (`RecipeCalculationService.java`) was extended to support custom recipe formulation with strict mathematical integrity:
1. **Raw Ingredient Aggregation:**
   - Raw grams $W_i$ are converted to standard 100g factors: $F_i = W_i / 100$.
   - Each nutrient $N$ is multiplied by ingredient-specific retention factor $R_i$:
     $$N_{\text{batch}} = \sum_i (N_i \times F_i \times R_i)$$
2. **Authoritative Final Cooked Weight vs. Yield Factor (No Double Counting):**
   - **Case A (User supplies measured final cooked weight):**
     If user specifies $W_{\text{cooked}}$, this measured weight is authoritative. The derived cooking yield factor is computed as:
     $$Y = \frac{W_{\text{cooked}}}{W_{\text{raw}}}$$
     No second yield factor is applied.
   - **Case B (User supplies yield factor):**
     If $W_{\text{cooked}}$ is absent, it is calculated as:
     $$W_{\text{cooked}} = W_{\text{raw}} \times Y$$
3. **Nutrient Densities & Per-Serving Metrics:**
   - **Per 100g Finished Dish:**
     $$N_{100g} = \frac{N_{\text{batch}}}{W_{\text{cooked}}} \times 100$$
   - **Weight per Serving:**
     $$W_{\text{serving}} = \frac{W_{\text{cooked}}}{S}$$
   - **Per-Serving Nutrients:**
     $$N_{\text{serving}} = N_{100g} \times \left(\frac{W_{\text{serving}}}{100}\right)$$
   - Both full batch totals and per-serving metrics reconcile within decimal rounding precision.

---

## 5. Schema Migrations & Changed Files

### Flyway Migration
- [`V6__custom_recipes_and_staple_ingredients.sql`](file:///d:/Projects/WebProgramming/src/main/resources/db/migration/V6__custom_recipes_and_staple_ingredients.sql):
  - Added columns `user_id` (`VARCHAR(100)`), `is_custom` (`BOOLEAN DEFAULT FALSE`), `is_active` (`BOOLEAN DEFAULT TRUE`), `description` (`TEXT`) to `recipes`.
  - Added indexes `idx_recipes_user_id` and `idx_recipes_is_custom`.
  - Inserted 7 IFCT staple raw ingredients (`food_items`, `food_nutrients`, `food_portions`).

### Backend Architecture
- [`Recipe.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/domain/entity/Recipe.java): Added custom recipe fields and active soft-delete flags.
- [`RecipeRepository.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/repository/RecipeRepository.java): Added user-scoped queries with ingredient eager fetch.
- [`CustomRecipeRequestDto.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/dto/CustomRecipeRequestDto.java): Request validation DTO for custom formulations.
- [`RecipeCalculationResultDto.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/dto/RecipeCalculationResultDto.java): Added batch totals and incomplete nutrient warning flags.
- [`RecipeCalculationService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/RecipeCalculationService.java): Refactored yield handling and batch totals.
- [`CustomRecipeService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/CustomRecipeService.java): Full CRUD, ownership enforcement, soft-deletion, and live preview calculation.
- [`CustomRecipeController.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/controller/CustomRecipeController.java): REST endpoints under `/api/custom-recipes/**`.
- [`MealLoggingService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/MealLoggingService.java): Enforced recipe ownership checks when logging custom recipes into daily meals.
- [`SecurityConfig.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/config/SecurityConfig.java): Restricted `/api/custom-recipes/**` to authenticated sessions.

### Frontend Integration (Approved Palette & UI)
- [`index.html`](file:///d:/Projects/WebProgramming/src/main/resources/static/index.html): Added tab toggle between Curated Recipes and My Custom Recipes, "+ Create Custom Recipe" CTA, custom recipes card container, and custom formulation modal (`#modal-custom-recipe`).
- [`styles.css`](file:///d:/Projects/WebProgramming/src/main/resources/static/css/styles.css): Added `.custom-recipe-card`, `.custom-recipe-metrics`, and `.empty-custom-recipes` strictly adhering to the approved warm ivory (`#F7F4ED`), soft white (`#FFFDF8`), charcoal (`#292722`), and muted terracotta (`#B85F45`) palette.
- [`app.js`](file:///d:/Projects/WebProgramming/src/main/resources/static/js/app.js): Custom recipe client state, live preview calculations, recipe logging to diary, and modal search integration.

---

## 6. Custom Recipe API Endpoints

All endpoints require an active authenticated session:

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/custom-recipes` | Creates a new user-owned custom recipe | `201 Created`, `400 Bad Request`, `401 Unauthorized` |
| `GET` | `/api/custom-recipes` | Lists all active custom recipes owned by the caller | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/custom-recipes/{id}` | Retrieves detail of an owned custom recipe | `200 OK`, `403 Forbidden`, `404 Not Found` |
| `PUT` | `/api/custom-recipes/{id}` | Updates an owned custom recipe | `200 OK`, `403 Forbidden`, `404 Not Found` |
| `DELETE` | `/api/custom-recipes/{id}` | Soft-deletes an owned custom recipe (`is_active = false`) | `204 No Content`, `403 Forbidden`, `404 Not Found` |
| `POST` | `/api/custom-recipes/preview` | Live in-memory nutrition preview calculation | `200 OK`, `400 Bad Request` |

---

## 7. Authorization & Ownership Behavior

1. **Principal-Derived Ownership:**
   - Ownership is strictly bound to `principal.getName()` from the Spring Security context.
   - Client-supplied user IDs in request bodies are ignored.
2. **Access Isolation (User A vs. User B):**
   - User B cannot read, list, modify, soft-delete, or log custom recipes belonging to User A.
   - Attempting to access another user's recipe returns `403 Forbidden` or `404 Not Found`.
3. **Canonical vs. Custom Recipe Access:**
   - Predefined system recipes (`is_custom = false`) remain readable by all authenticated users.
   - Custom recipes (`is_custom = true`) are strictly private to their owner.
4. **Historical Snapshot Preservation:**
   - When a recipe is modified or soft-deleted, previously logged entries in `meal_entries` preserve the exact nutrient snapshot (`energy_kcal`, `protein_g`, `carbohydrates_g`, `fat_g`, `fiber_g`) calculated at log creation time.

---

## 8. Automated Test Results

A dedicated test suite [`CustomRecipeAndDataQualityTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/CustomRecipeAndDataQualityTest.java) was added covering:
- Nutrition data provenance and genuine zeros vs nulls.
- Single and multi-ingredient yield calculations.
- Cooking yield without double-counting when cooked weight is supplied.
- Custom recipe creation, retrieval, updating, and soft-deletion.
- Strict ownership isolation between User A and User B.
- Historical meal entry snapshot immutability upon recipe modification.
- Validation rejections for empty recipes and negative quantities.

### Full Maven Test Run
```text
[INFO] Running com.fitkit.CustomRecipeAndDataQualityTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.145 s -- in com.fitkit.CustomRecipeAndDataQualityTest
...
[INFO] Running com.fitkit.SecurityAndAccountIsolationTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.748 s -- in com.fitkit.SecurityAndAccountIsolationTest
...
[INFO] Running com.fitkit.WebSecurityEndpointProtectionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.909 s -- in com.fitkit.WebSecurityEndpointProtectionTest
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  32.088 s
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
```
**All 46 unit and integration tests passed with 0 failures, 0 errors, and 0 skipped.**

---

## 9. MySQL Migration & Integration Status

- **Database:** Local MySQL 8.0 `fitkit_db` on `localhost:3306`.
- **Flyway Status:** Migration `V6__custom_recipes_and_staple_ingredients.sql` successfully applied against MySQL.
- **Application Startup:** Tomcat initialized on port 8080 with 33 catalog foods and 3 canonical recipes.
- **End-to-End Verification:**
  - Browser subagent created "Homemade Vegetable Upma" from raw semolina, onion, tomato, and ghee.
  - Calculated batch and serving metrics (233 kcal, 5.7g protein, 40.1g carbs, 5.5g fat).
  - Logged 1 serving (105g) to Breakfast.
  - Verified Daily Overview updated consumed calories to 947 kcal.
  - Edited custom recipe title to "Special Homestyle Vegetable Upma".
  - Verified Daily Overview historical Breakfast entry remained intact with 233 kcal and original snapshot metadata.

---

## 10. Secret-Management Cleanup Status

- **Sanitization:** Sanitized hardcoded database passwords across all documentation and configuration templates.
- **Configuration Hygiene:** `src/main/resources/application.properties` strictly requires `${DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD}}` and fails fast if unset. No plaintext fallbacks exist.
- **Demo Account Seeding:** Restricted `fitkit.auth.seed-demo=${SEED_DEMO_ACCOUNT:false}` to default to `false`. Explicitly enabled in development via `application-dev.properties` or environment variable.
- **Logs Inspection:** Verified that no plaintext passwords, session secrets, or tokens appear in application startup logs or surefire test reports.

---

## 11. Known Limitations & Recommended Next Steps

1. **Micronutrient Rollups in Custom Recipes:** The current Phase 6 implementation computes prox-macros (energy, protein, carbohydrates, fat, dietary fiber, moisture, and ash). Future phases can expand calculations to micronutrients (iron, calcium, sodium, potassium).
2. **Recipe Duplication ("Save as Copy"):** Adding a single-click "Duplicate / Fork Recipe" feature will let users clone canonical recipes (e.g. Dal Tadka) into their custom space for quick customization.
3. **Cooking Retention Factor Presets:** Currently defaults to 1.0 or user override; can be enriched with standard USDA Table of Nutrient Retention Factors based on selected cooking method.
