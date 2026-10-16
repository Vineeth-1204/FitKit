# FitKit Phase 4 — Implementation Audit & Architecture Plan

## 1. Executive Summary

An exhaustive technical audit of the FitKit application was conducted across the frontend client (`app.js`, `index.html`, `styles.css`), backend services (`com.fitkit.*`), Flyway database migrations (`V1`, `V2`), and automated tests.

The application possesses a solid nutritional computation and provenance foundation (verified source metadata, raw/cooked distinctions, yield and nutrient retention modeling, and 21 passing automated tests against H2 and MySQL 8.0). However, **meal diary logging, daily nutrient totals, and daily targets currently rely entirely on browser `localStorage`**. The backend currently has no persistence tables or API endpoints for meal journals, meal entries, or user target settings.

This audit documents verified existing functionality, missing architecture components, database changes required, security and integrity risks, and a concrete phased implementation plan for Phase 4.

---

## 2. Existing Functionality Verified in Source Code

### 2.1 Backend & Database
- **Canonical Schema (`V1__init_nutrition_schema.sql`)**:
  - `source_provenance`: Authoritative citation, publisher, license (`IFCT_2017`, `USDA_FDC`, `OPEN_FOOD_FACTS`, `FITKIT_RECIPE_ENGINE`).
  - `foods`: Canonical food items with `food_code` (unique constraint), `source_id`, `food_state` (`RAW_INGREDIENT`, `COOKED_SINGLE`, `PREPARED_RECIPE`, `PACKAGED_COMMERCIAL`), verification status, and timestamps.
  - `food_nutrients`: Strict per-100g energy (kcal/kJ), protein, carbohydrates, total fat, dietary fiber, water, ash, and quality status tracking (`SOURCE_VERIFIED`, `CALCULATED_ESTIMATE`, `CORRECTED_ANOMALY`). Unique constraint on `(food_id, basis)`.
  - `food_portions`: Standard household measurements (cups, katoris, slices, pieces) with explicit gram weights.
  - `recipes` & `recipe_ingredients`: Yield factors, cooking methods, raw ingredient gram weights, retention factor overrides, and serving division.
  - `import_audit_logs`: Audit trail for data imports.
- **Data Seed (`V2__seed_sources_and_sample_catalog.sql`)**:
  - 11 verified food items and 3 standardized composite recipes (`Traditional Whole Wheat Chapati`, `Steamed Idli`, `Dal Tadka`).
  - Provenance-backed nutrient profiles with anomaly correction (e.g. Ghee 0 kJ corrected via 9.0 kcal/g standard).
- **APIs**:
  - `FoodCatalogController`: `/api/foods` (search by query, state, group), `/api/foods/{id}`, `/api/foods/groups`, `/api/foods/states`.
  - `RecipeController`: `/api/recipes`, `/api/recipes/{id}`, `/api/recipes/{id}/calculate`, `/api/recipes/custom-calculate`.
  - `SystemStatsController`: `/api/stats` reporting catalog size, verified items, and active recipes.
- **Calculation Services**:
  - `NormalizationService`: Unit conversions, Atwater factors, macronutrient sum validation, and restricted anomaly corrections.
  - `RecipeCalculationService`: Deterministic recipe nutrition synthesis accounting for raw ingredient summation, water yield expansion/shrinkage, and retention factors.
- **Test Suite**:
  - 21 automated JUnit 5 tests covering database constraints, normalization rules, and recipe calculations passing cleanly.

### 2.2 Frontend Client
- **Approved Editorial Design System**: Warm ivory (`#F7F4ED`), soft white (`#FFFDF8`), charcoal (`#292722`), muted terracotta (`#B85F45`), warm stone (`#E5DED2`). Responsive layout with sidebar navigation, summary cards, and modals.
- **Date Navigation**: Next day, previous day, and jump-to-today controls updating `state.currentDate`.
- **Food Library Table**: Connected to `/api/foods`, supporting text search, food state tabs, and food category filtering.
- **Recipe Lab**: Connected to `/api/recipes`, interactive yield factor and serving sliders invoking `/api/recipes/{id}/calculate` via `POST`.
- **Food Inspection Modal**: Comprehensive nutrient breakdown per 100g and per portion with citation metadata.

---

## 3. Missing or Incomplete Functionality

| Area | Current Implementation | Missing / Incomplete Requirements |
| :--- | :--- | :--- |
| **Meal Logging Persistence** | Client `localStorage.getItem('fitkit_diary_YYYY-MM-DD')` | No backend tables or REST endpoints exist. Logs are lost across browsers/devices. |
| **Nutrient Snapshots** | Calculated on the fly in `app.js` using current catalog data | If a food's nutritional values are updated in the catalog, historical meal logs will be retroactively distorted unless a snapshot of nutrient values at time of logging is saved. |
| **Meal Editing** | Only deletion (`window.FitKitApp.deleteDiaryEntry`) exists; no edit quantity/portion flow | Inability to edit portion size or multiplier without deleting and recreating the entry. |
| **Daily Target Persistence** | Client `localStorage.getItem('fitkit_targets')` | Targets are not stored on the server. If a user changes targets on one device, other sessions do not reflect it. |
| **Progress Chart History** | Iterates 7 days backwards querying `localStorage` | Historical chart is limited to local cache; does not pull authoritative multi-day summaries from the backend. |
| **Catalog Depth** | 11 foods (8 ingredients + 3 prepared recipes) | Common everyday Indian staples (dosa, poha, paneer dishes, sambar, chicken, eggs, fruits, curd) are missing. |
| **User Identity & Isolation** | No user concept exists (neither in schema nor API) | No user identifier column on meals or targets. |
| **Safe Data Migration** | Hardcoded sample diary inserted on today's date if empty | No validation or migration bridge from existing `localStorage` to server. |

---

## 4. Existing Components That Can Be Reused

1. **`FoodCatalogController` & `RecipeController`**:
   - Reused as-is for search, detail retrieval, and recipe calculation.
2. **`NormalizationService` & `RecipeCalculationService`**:
   - Reused for validating nutrient inputs, Atwater checks, and recipe nutrient synthesis.
3. **Frontend UI Components (`index.html` & `styles.css`)**:
   - Approved editorial styling, layout, meal cards (`Breakfast`, `Lunch`, `Dinner`, `Snacks`), modals, and progress bars are 100% retained without visual alteration.
4. **`app.js` UI Rendering & DOM Binding Logic**:
   - `renderDailyOverview()`, `updateMacroBar()`, `renderWeeklyTrends()`, and modal flows will be connected to backend fetch calls rather than `localStorage`.

---

## 5. Database Changes Required (Migration `V3` & `V4`)

### 5.1 Migration V3: Meal Logging & Target Entities
To support multi-day meal tracking and historical integrity without breaking existing schemas:

1. **`user_targets` Table**:
   - `id BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `user_id VARCHAR(100) NOT NULL UNIQUE` (default `'default-user'` for single-user dev mode)
   - `daily_calories INT NOT NULL DEFAULT 2000`
   - `protein_g DECIMAL(6, 1) NOT NULL DEFAULT 75.0`
   - `carbs_g DECIMAL(6, 1) NOT NULL DEFAULT 250.0`
   - `fat_g DECIMAL(6, 1) NOT NULL DEFAULT 65.0`
   - `fiber_g DECIMAL(6, 1) NOT NULL DEFAULT 30.0`
   - `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

2. **`meal_logs` Table**:
   - `id BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `user_id VARCHAR(100) NOT NULL`
   - `log_date DATE NOT NULL`
   - `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
   - `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
   - `CONSTRAINT uq_user_log_date UNIQUE (user_id, log_date)`
   - Index on `(user_id, log_date)`

3. **`meal_entries` Table**:
   - `id BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `meal_log_id BIGINT NOT NULL`
   - `meal_type VARCHAR(30) NOT NULL` (`BREAKFAST`, `LUNCH`, `DINNER`, `SNACKS`)
   - `food_id BIGINT NULL` (Foreign key to `foods(id)` ON DELETE SET NULL)
   - `recipe_id BIGINT NULL` (Foreign key to `recipes(id)` ON DELETE SET NULL)
   - `food_name VARCHAR(255) NOT NULL` (Captured name)
   - `food_state VARCHAR(50) NOT NULL`
   - `portion_description VARCHAR(150) NOT NULL`
   - `portion_gram_weight DECIMAL(8, 2) NOT NULL`
   - `quantity_multiplier DECIMAL(6, 2) NOT NULL DEFAULT 1.00`
   - `total_gram_weight DECIMAL(8, 2) NOT NULL`
   - **Nutrient Snapshots (Authoritative historical record)**:
     - `energy_kcal DECIMAL(8, 2) NOT NULL`
     - `energy_kj DECIMAL(8, 2)`
     - `protein_g DECIMAL(8, 2) NOT NULL`
     - `carbohydrates_g DECIMAL(8, 2) NOT NULL`
     - `fat_g DECIMAL(8, 2) NOT NULL`
     - `fiber_g DECIMAL(8, 2) NOT NULL DEFAULT 0.00`
   - `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
   - `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
   - `CONSTRAINT fk_entries_log FOREIGN KEY (meal_log_id) REFERENCES meal_logs(id) ON DELETE CASCADE`
   - Indexes on `meal_log_id` and `meal_type`

### 5.2 Migration V4: Food Catalog Expansion
Seeding authentic, verified Indian staple foods and portions with IFCT 2017 and USDA references:
- **Cooked Rice (Basmati / Indian standard)**
- **Dosa (Fermented Rice & Black Gram Crepe)**
- **Poha (Flattened rice cooked with turmeric and seasonings)**
- **Sambar (Toor dal, drumstick & mixed vegetables stew)**
- **Moong Dal (Raw split pulse & Cooked yellow dal)**
- **Curd / Dahi (Buffalo & Cow milk standard)**
- **Whole Cow's Milk**
- **Boiled Whole Chicken Egg**
- **Chicken Breast (Cooked, skinless)**
- **Rohu / Freshwater Fish (Cooked)**
- **Palak / Spinach (Raw & Steamed)**
- **Potato (Boiled)**
- **Banana (Robusta)**
- **Apple (Standard)**

---

## 6. Risks & Safeguards

1. **Retroactive Nutrition Distortion (High Risk)**:
   - *Risk*: If meal entries only store a foreign key `food_id` and recalculate totals dynamically from current catalog rows, any future correction to catalog data will silently corrupt past diary entries.
   - *Safeguard*: Every logged `meal_entry` stores an explicit **nutrient snapshot** calculated authoritatively by the backend at submission time.
2. **Double Yield Application on Recipes (Moderate Risk)**:
   - *Risk*: A composite recipe (`PREPARED_RECIPE`) already has yield adjustments factored into its per-serving or per-100g values. If a consumer treats it as raw ingredients and re-applies yield factors, calories are severely undercounted.
   - *Safeguard*: Distinct entity flags (`food_id` vs `recipe_id` and `food_state = PREPARED_RECIPE`). Recipes use the computed per-serving or per-100g profile directly.
3. **Data Loss During Browser Storage Migration (High Risk)**:
   - *Risk*: Users with existing entries in `localStorage` could lose their journals when switching to server persistence.
   - *Safeguard*: Implement an idempotent, safe client migration function that checks for existing `fitkit_diary_*` items in `localStorage`, posts them to `/api/meals/migrate`, and only marks them as migrated once the server responds with HTTP 200.
4. **Missing Nutrient Values vs Zeroes (Moderate Risk)**:
   - *Risk*: In analytical food science, an unmeasured nutrient (e.g. unknown micronutrient or missing fiber figure) is distinct from 0.00g.
   - *Safeguard*: Maintain nullable columns in `food_nutrients` and use `null` to denote unanalyzed values while reserving `0.00` for verified absences.
5. **Multi-User Isolation & Security (Low/Moderate Risk)**:
   - *Risk*: Without authentication, trusting an arbitrary `user_id` could cause collision or unauthorized overwrites.
   - *Safeguard*: For Phase 4 development, isolate records under a deterministic user session identifier (`"default-user"` or client-generated UUID stored in cookie/localStorage), enforce validation on all inputs, and clearly document the scope without claiming production multi-tenant auth.

---

## 7. Prioritized Implementation Plan

1. **Step 1 (Database & Entities)**:
   - Write Flyway migration `V3__meal_logging_and_user_targets.sql`.
   - Implement entities: `MealLog`, `MealEntry`, `UserTarget`, `MealType`.
   - Implement repositories: `MealLogRepository`, `MealEntryRepository`, `UserTargetRepository`.
2. **Step 2 (Backend Service & APIs)**:
   - Create `MealLoggingService` with transactional operations:
     - `getMealLog(userId, date)`: Returns meal entries grouped by meal category, meal subtotals, and daily totals.
     - `addMealEntry(...)`: Computes nutrient snapshot from food or recipe reference and saves entry.
     - `updateMealEntry(entryId, quantityMultiplier, portionId)`: Recalculates snapshot and updates entry.
     - `deleteMealEntry(entryId)`: Deletes entry and updates parent log.
     - `getDailyHistory(userId, startDate, endDate)`: Provides time-series totals for progress charts.
   - Create `UserTargetService` to fetch and update calorie/macro targets.
   - Create `MealLogController` (`/api/meals`) and `UserTargetController` (`/api/targets`).
3. **Step 3 (Catalog Expansion)**:
   - Write Flyway migration `V4__expand_indian_food_catalog.sql` adding verified IFCT/USDA items.
4. **Step 4 (Frontend Integration)**:
   - Connect `app.js` to `/api/meals` and `/api/targets`.
   - Implement entry editing (quantity adjustment modal/control) alongside deletion.
   - Implement safe migration for `localStorage` diaries.
   - Connect Progress view to `/api/meals/history`.
5. **Step 5 (Automated Testing & Verification)**:
   - Add integration tests for meal logging, nutrient snapshot integrity, and target updates.
   - Execute test suite (`mvn test`) and verify Flyway against MySQL 8.0.
   - Browser subagent validation of full logging, editing, and progress workflows.
6. **Step 6 (Implementation Report)**:
   - Compile `docs/phase-4-implementation-report.md`.
