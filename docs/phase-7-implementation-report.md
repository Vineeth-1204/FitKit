# FitKit — Phase 7 Implementation Report: Food Catalog Expansion & Smarter Food Search

## 1. Executive Summary

FitKit Phase 7 delivers a comprehensive expansion of the Indian food catalog and a high-precision, deterministic search engine with multilingual Hindi/regional alias support, multi-criteria filtering, and transparent nutrition provenance indicators.

Key achievements in Phase 7:
1. **Catalog Expansion (18 New Indian Staples)**: Expanded the authoritative catalog from 33 items to 51 items with traceable nutrition data from the **Indian Food Composition Tables (IFCT 2017, ICMR-NIN)**. Foods cover essential everyday Indian staples across grains, pulses, aromatics, leafy vegetables, roots, tubers, and souring agents.
2. **Search Aliases (`food_aliases`)**: Implemented a dedicated normalized table and bidirectional mapping supporting Hindi, Tamil, Telugu, Kannada, Gujarati, and common transliterations (`suji`, `rava`, `semolina`, `atta`, `dahi`, `jeera`, `haldi`, `pyaaz`, `tamatar`, `aloo`, `adrak`, `lahsun`, `baingan`, `kadi patta`, `chana`, `rajma`, `besan`, etc.).
3. **Deterministic 6-Tier Relevance Ranking**: Developed a predictable ranking hierarchy:
   - Rank 1: Exact canonical name match
   - Rank 2: Exact alias match
   - Rank 3: Canonical name starts with query
   - Rank 4: Alias starts with query
   - Rank 5: Partial canonical name match
   - Rank 6: Partial alias or scientific name or food code match
   - Result deduplication across multiple aliases matching the same food.
4. **Practical Food Library Filters**: Integrated Food State (`RAW_INGREDIENT`, `COOKED_SINGLE`, `PREPARED_RECIPE`, `PACKAGED_COMMERCIAL`), Food Category (`foodGroup`), Data Quality Status (`SOURCE_VERIFIED`, `CORRECTED_ANOMALY`, `CALCULATED_ESTIMATE`, `UNVERIFIED`), Sorting modes (`RELEVANCE`, `NAME_ASC`, `NAME_DESC`, `CALORIES_DESC`, `CALORIES_ASC`), and a single-click "Clear" filter reset.
5. **Nutrition Provenance & Quality Indicators**: Embedded restrained, editorial badges displaying data reliability (`Source Verified`, `Audit Corrected`, `Calculated`, `Needs Verification`) and matched alias tags (`Matched: "suji"`) adhering strictly to the warm ivory (`#F7F4ED`), soft white (`#FFFDF8`), charcoal (`#292722`), and muted terracotta (`#B85F45`) design system.
6. **Dual Database Verification**: Validated full forward-only Flyway migration `V8__expand_food_catalog_and_search_aliases.sql` across both in-memory H2 (70/70 tests passing) and live local MySQL 8.0 (`fitkit_db`, upgraded from v7 to v8 without data loss).

---

## 2. Repository Audit Findings

Prior to making changes, an internal audit was performed:
- **Entities & Schema**: `FoodItem`, `FoodNutrient`, and `FoodPortion` contained robust foundational structures. However, alias search previously relied on substring filtering against `name`, `scientificName`, and `brandName`. There was no schema representation for regional synonyms or transliterations.
- **Migration History**: Applied migrations `V1` through `V7` were left completely intact and immutable. Migration `V7` had reconciled 7 staple foods (Semolina to A022, Onion to G017, Mustard Oil to T017 with FAO factor 8.84 kcal/g).
- **Search Controller & Services**: `FoodCatalogController.getFoods` handled basic in-memory filtering. Delegating search to a dedicated `FoodSearchService` enables testable, reusable ranking and query normalization.
- **UI State & Controls**: The Food Library view (`#view-library`) had search and food state pills, but lacked data quality status filters, sort dropdowns, clear filter actions, and alias indicator badges.

---

## 3. Catalog Additions and Authoritative Sources

All 18 new staples were sourced directly from **IFCT 2017 (National Institute of Nutrition, Indian Council of Medical Research)**:

| ID | Food Code | Source Code | Food Name | Scientific Name | Food Group | State | Energy (kcal) | Energy (kJ) | Protein (g) | Carbs (g) | Fat (g) | Fiber (g) | Quality Status | Default Portion |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 34 | `IFCT_A008` | `A008` | Brown Rice, raw grain | *Oryza sativa* | Cereal Grains and Pastas | RAW | 353.20 | 1478.00 | 7.94 | 70.82 | 2.75 | 4.80 | SOURCE_VERIFIED | 1/2 cup raw (90g) |
| 35 | `IFCT_A015` | `A015` | Rolled Oats / Oatmeal, raw flakes | *Avena sativa* | Cereal Grains and Pastas | RAW | 374.30 | 1566.00 | 13.15 | 63.30 | 6.90 | 10.40 | SOURCE_VERIFIED | 1/2 cup dry (40g) |
| 36 | `IFCT_B006` | `B006` | Besan / Bengal Gram Flour | *Cicer arietinum* | Grain Legumes and Pulses | RAW | 365.70 | 1530.00 | 21.80 | 56.60 | 5.30 | 12.80 | SOURCE_VERIFIED | 1/4 cup dry (30g) |
| 37 | `IFCT_A011` | `A011` | Raw Poha / Flattened Rice Flakes | *Oryza sativa* | Cereal Grains and Pastas | RAW | 354.00 | 1481.00 | 6.70 | 77.20 | 1.20 | 2.80 | SOURCE_VERIFIED | 1/2 cup dry (40g) |
| 38 | `IFCT_B005` | `B005` | Chana Dal / Split Bengal Gram, raw | *Cicer arietinum* | Grain Legumes and Pulses | RAW | 370.00 | 1548.00 | 21.55 | 57.82 | 5.30 | 15.20 | SOURCE_VERIFIED | 1/2 cup raw (100g) |
| 39 | `IFCT_B008` | `B008` | Kabuli Chana / White Chickpeas, raw | *Cicer arietinum* | Grain Legumes and Pulses | RAW | 362.10 | 1515.00 | 20.47 | 57.43 | 5.48 | 17.40 | SOURCE_VERIFIED | 1/2 cup raw (100g) |
| 40 | `IFCT_B018` | `B018` | Rajma / Red Kidney Beans, raw | *Phaseolus vulgaris* | Grain Legumes and Pulses | RAW | 346.60 | 1450.00 | 22.90 | 58.60 | 1.30 | 24.90 | SOURCE_VERIFIED | 1/2 cup raw (90g) |
| 41 | `IFCT_G010` | `G010` | Ginger, fresh raw (Adrak) | *Zingiber officinale* | Condiments and Spices | RAW | 55.00 | 230.00 | 1.82 | 10.35 | 0.75 | 2.40 | SOURCE_VERIFIED | 1 inch piece (10g) |
| 42 | `IFCT_G009` | `G009` | Garlic, fresh raw (Lahsun) | *Allium sativum* | Condiments and Spices | RAW | 125.00 | 523.00 | 6.30 | 22.80 | 0.50 | 2.10 | SOURCE_VERIFIED | 3-4 cloves (12g) |
| 43 | `IFCT_C009` | `C009` | Curry Leaves, fresh (Kadi Patta) | *Murraya koenigii* | Green Leafy Vegetables | RAW | 108.00 | 452.00 | 6.10 | 15.20 | 1.00 | 6.40 | SOURCE_VERIFIED | 1 sprig (5g) |
| 44 | `IFCT_C008` | `C008` | Coriander Leaves, fresh (Hara Dhaniya) | *Coriandrum sativum* | Green Leafy Vegetables | RAW | 31.10 | 130.00 | 3.30 | 2.40 | 0.60 | 4.20 | SOURCE_VERIFIED | 2 tbsp chopped (10g) |
| 45 | `IFCT_D014` | `D014` | Carrot, fresh raw (Gajar) | *Daucus carota* | Other Vegetables | RAW | 32.50 | 136.00 | 0.90 | 6.40 | 0.20 | 2.80 | SOURCE_VERIFIED | 1 medium carrot (80g) |
| 46 | `IFCT_D008` | `D008` | Brinjal / Eggplant, fresh raw (Baingan) | *Solanum melongena* | Other Vegetables | RAW | 25.10 | 105.00 | 1.40 | 3.80 | 0.30 | 3.40 | SOURCE_VERIFIED | 1 cup diced (100g) |
| 47 | `IFCT_C019` | `C019` | Spinach / Palak, fresh raw leaves | *Spinacia oleracea* | Green Leafy Vegetables | RAW | 26.10 | 109.00 | 2.00 | 2.90 | 0.70 | 2.20 | SOURCE_VERIFIED | 1 cup raw leaves (50g) |
| 48 | `IFCT_E010` | `E010` | Potato, fresh raw (Aloo) | *Solanum tuberosum* | Roots and Tubers | RAW | 70.00 | 293.00 | 1.60 | 15.20 | 0.10 | 1.70 | SOURCE_VERIFIED | 1 medium potato (120g) |
| 49 | `IFCT_F012` | `F012` | Coconut Kernel, fresh raw (Nariyal) | *Cocos nucifera* | Nuts and Seeds | RAW | 354.00 | 1481.00 | 3.33 | 7.25 | 33.49 | 9.00 | SOURCE_VERIFIED | 2 tbsp grated (20g) |
| 50 | `IFCT_F023` | `F023` | Lemon / Lime, fresh raw (Nimbu) | *Citrus aurantifolia* | Fruits | RAW | 27.00 | 113.00 | 0.70 | 5.70 | 0.10 | 1.30 | SOURCE_VERIFIED | Juice of 1 lemon (30g) |
| 51 | `IFCT_G031` | `G031` | Tamarind, raw pulp (Imli) | *Tamarindus indica* | Condiments and Spices | RAW | 240.00 | 1004.00 | 3.10 | 56.70 | 0.10 | 5.60 | SOURCE_VERIFIED | 1 marble ball (5g) |

---

## 4. Search Ranking and Alias Implementation

### `food_aliases` Schema
```sql
CREATE TABLE IF NOT EXISTS food_aliases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    alias_name VARCHAR(150) NOT NULL,
    language_or_dialect VARCHAR(50) DEFAULT 'COMMON',
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aliases_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);
CREATE INDEX idx_food_aliases_food_id ON food_aliases(food_id);
CREATE INDEX idx_food_aliases_name ON food_aliases(alias_name);
```

### Deterministic 6-Tier Relevance Hierarchy (`FoodSearchService.java`)
1. **Rank 1**: Exact match with canonical name (`item.getName()`)
2. **Rank 2**: Exact match with any associated alias (`alias.getAliasName()`)
3. **Rank 3**: Canonical name starts with query
4. **Rank 4**: Any alias starts with query
5. **Rank 5**: Canonical name contains query
6. **Rank 6**: Any alias contains query, or scientific name, food code, or brand name contains query.

Foods are sorted primarily by `rank ASC` and secondarily by canonical `name ASC` (case-insensitive). Deduplication ensures a food matching multiple aliases (e.g. `suji`, `rava`, `bombay rava`) appears exactly once, displaying the matched alias on the card.

---

## 5. Practical Filters Implemented

The Food Library interface (`#view-library`) provides four orthogonal filter criteria:
1. **Food State**: `ALL`, `RAW_INGREDIENT`, `COOKED_SINGLE`, `PREPARED_RECIPE`, `PACKAGED_COMMERCIAL`.
2. **Food Category**: Dynamically populated from distinct `foodGroup` values in the database.
3. **Data Quality Status**: `ALL`, `SOURCE_VERIFIED`, `CORRECTED_ANOMALY`, `CALCULATED_ESTIMATE`, `UNVERIFIED`.
4. **Sort Order**: `RELEVANCE`, `NAME_ASC` (A–Z), `NAME_DESC` (Z–A), `CALORIES_DESC` (High–Low), `CALORIES_ASC` (Low–High).
5. **Clear Filters**: Instant reset of search query, state pills, dropdowns, and results back to the 51-item catalog baseline.

---

## 6. Nutrition Provenance and Quality Indicators

Editorial badges were implemented in `styles.css` adhering strictly to approved palette constraints:
- **Source Verified** (`.quality-verified`): `#EDF3EC` background, `#2E5C38` olive text, subtle green border.
- **Audit Corrected** (`.quality-corrected`): `#FDF4EB` background, `#9E4F38` terracotta text, subtle border.
- **Calculated Estimate** (`.quality-estimate`): `#F5EFE6` warm sand background, `#777168` charcoal gray text.
- **Needs Verification** (`.quality-unverified`): `#FBEBE8` soft rose background, `#A84438` brick text.
- **Matched Alias** (`.alias-tag`): `#F7EFE8` background, `#B85F45` terracotta text, 4px border radius.

---

## 7. Files Changed and Created

### Backend:
- [`src/main/resources/db/migration/V8__expand_food_catalog_and_search_aliases.sql`](file:///d:/Projects/WebProgramming/src/main/resources/db/migration/V8__expand_food_catalog_and_search_aliases.sql): Forward-only Flyway migration creating `food_aliases`, seeding 18 foods, and seeding aliases for foods 1–51.
- [`src/main/java/com/fitkit/domain/entity/FoodAlias.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/domain/entity/FoodAlias.java): JPA entity for aliases.
- [`src/main/java/com/fitkit/domain/entity/FoodItem.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/domain/entity/FoodItem.java): Added `@OneToMany List<FoodAlias> aliases` with Hibernate `@BatchSize(size = 100)` and helper getters.
- [`src/main/java/com/fitkit/dto/FoodSummaryDto.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/dto/FoodSummaryDto.java): Added `aliases` (`List<String>`) and `matchedAlias` (`String`).
- [`src/main/java/com/fitkit/repository/FoodItemRepository.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/repository/FoodItemRepository.java): Added `findAllWithAliasesAndSource()` query method.
- [`src/main/java/com/fitkit/service/FoodSearchService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/FoodSearchService.java): Core search ranking, alias matching, multi-criteria filtering, and sorting logic.
- [`src/main/java/com/fitkit/controller/FoodCatalogController.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/controller/FoodCatalogController.java): Injected `FoodSearchService`, added `quality` and `sort` parameters to `GET /api/foods`, added `GET /api/foods/qualities`.

### Frontend:
- [`src/main/resources/static/index.html`](file:///d:/Projects/WebProgramming/src/main/resources/static/index.html): Added `#lib-quality-select`, `#lib-sort-select`, and `#btn-clear-lib-filters` controls.
- [`src/main/resources/static/css/styles.css`](file:///d:/Projects/WebProgramming/src/main/resources/static/css/styles.css): Added `.quality-badge`, `.quality-verified`, `.quality-corrected`, `.quality-estimate`, `.quality-unverified`, `.alias-tag`, and `.alias-hint` styles.
- [`src/main/resources/static/js/app.js`](file:///d:/Projects/WebProgramming/src/main/resources/static/js/app.js): Updated `loadFoods`, `renderLibraryTable`, `applyLibraryFilters`, modal search debounced API fetching, and clear filters event handlers.

### Automated Tests:
- [`src/test/java/com/fitkit/FoodSearchAndCatalogExpansionTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/FoodSearchAndCatalogExpansionTest.java): Comprehensive 21-test suite covering catalog expansion, 6-tier ranking, deduplication, filters, and Phase 6.1 preservation.

---

## 8. Database Migrations Created

**Migration file**: `V8__expand_food_catalog_and_search_aliases.sql`
- Creates table `food_aliases` with foreign key cascade to `foods(id)` and B-tree indexes on `food_id` and `alias_name`.
- Inserts 18 new foods (IDs 34 to 51) with complete nutrient profiles (`food_nutrients`) and realistic household portions (`food_portions`).
- Seeds 150+ aliases across English, Hindi, Tamil, Telugu, Kannada, Gujarati, and regional dialects for foods 1 through 51.

---

## 9. Automated Test Results

Running the full Maven test suite:
```
[INFO] Results:
[INFO] 
[INFO] Tests run: 70, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  16.201 s
```

Suite breakdown:
- `FoodSearchAndCatalogExpansionTest`: 21 tests (Catalog expansion, nutrition accuracy, 6-tier ranking, deduplication, state/group/quality filters, sorting)
- `CustomRecipeAndDataQualityTest`: 12 tests (Recipe yield calculations, custom recipes, meal logging snapshots)
- `SecurityAndAccountIsolationTest`: 8 tests (Session auth, isolation, CSRF protection)
- `WebSecurityEndpointProtectionTest`: 5 tests (Endpoint protection and unauthorized redirects)
- `MealLoggingIntegrationTest`: 8 tests (Daily meal journal, nutrient calculations)
- `NutritionNormalizationTest`: 6 tests (Energy kJ to kcal conversion, Atwater verification)
- `RecipeCalculationTest`: 6 tests (Yield factors, ingredient moisture loss)
- `DataImportPipelineTest`: 2 tests (Data foundation validation)
- `DatabaseConstraintsTest`: 2 tests (Unique constraints and foreign keys)

---

## 10. MySQL Migration Verification Evidence

Live migration output on local MySQL 8.0 (`fitkit_db`):
```
2026-10-15T23:44:51.826+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:mysql://localhost:3306/fitkit_db (MySQL 8.0)
2026-10-15T23:44:51.874+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 8 migrations (execution time 00:00.026s)
2026-10-15T23:44:51.887+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema `fitkit_db`: 7
2026-10-15T23:44:51.918+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Migrating schema `fitkit_db` to version "8 - expand food catalog and search aliases"
2026-10-15T23:44:52.093+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema `fitkit_db`, now at version v8 (execution time 00:00.148s)
2026-10-15T23:44:54.915+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] com.fitkit.runner.DataImportRunner       : Total Food Items in Catalog: 51
2026-10-15T23:44:54.919+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] com.fitkit.runner.DataImportRunner       : Total Nutrient Profiles: 51
2026-10-15T23:44:54.921+05:30  INFO 3564 --- [fitkit-nutrition-core] [           main] com.fitkit.runner.DataImportRunner       : Total Documented Recipes: 4
```

---

## 11. Browser / UI Verification

The browser subagent performed end-to-end user journey verification on `http://localhost:8080/`:
1. **Authentication**: Signed in successfully via `Demo Login` (`demo@fitkit.local`).
2. **Alias Search**:
   - `suji` -> returned *Semolina / Suji / Rava* with tag `Matched: "suji"`.
   - `pyaaz` -> returned *Onion, fresh raw* with tag `Matched: "pyaaz"`.
   - `tamatar` -> returned *Tomato, ripe raw* with tag `Matched: "tamatar"`.
   - `besan` -> returned *Besan / Bengal Gram Flour*.
3. **Quality Status Filtering**: Filtered by *Audit Corrected* -> isolated *Mustard Oil* and *Ghee* with `.quality-corrected` badge.
4. **Calorie Sorting**: Sorted by *Calories (High–Low)* -> *Ghee* (900 kcal), *Mustard Oil* (884 kcal), and *Amul Butter* (714 kcal) topped the list.
5. **Clear Filters**: Single click reset all state pills, category selects, quality selects, sort order, and returned all 51 items.
6. **Modal Search**: In the *Add Food* dialog, searching `suji` retrieved *Semolina / Suji / Rava* with matched alias tags directly through the backend API.
7. **Session Recording**: Recorded session WebP artifact: `phase7_food_search_verification_1792088113818.webp`.

---

## 12. Remaining Limitations and Recommended Next Steps

1. **Phonetic & Levenshtein Matching**: The current search system supports case-insensitive exact, prefix, and partial string matching across canonical names and regional aliases. For future phases, a phonetic algorithm (Double Metaphone or Soundex adapted for Indic transliterations) can be added for loose typing (e.g., `chawal` vs `chaawal`).
2. **User-Contributed Aliases**: Currently aliases are managed through the catalog migration. Allowing users to bookmark or attach personal nickname aliases to existing items would further personalize food logging.
3. **Pagination for Very Large Catalogs**: The catalog of 51 items renders instantaneously with zero perceptible delay. If the catalog expands into several thousand commercial items, server-side `Pageable` pagination should be added.
