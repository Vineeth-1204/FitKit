# FitKit Phase 6 — Nutrition Data Quality Audit & Standards

## 1. Executive Summary

Nutrition data reliability is the foundation of FitKit. Tracking calories and macronutrients without verified food composition data leads to compounding estimation errors that undermine user health outcomes.

This document presents the Phase 6 quality audit of FitKit's food catalog, provenance tracking, measurement units, food-state classifications, nutrient-value handling (distinguishing missing values from genuine zeros), and recipe calculation consistency. It also defines the catalog additions required for user-owned custom recipe creation.

---

## 2. Provenance Architecture & Standards

FitKit enforces strict citation and provenance tracking. Every food item must reference a registered `SourceProvenance` record:
- **`IFCT_2017`**: Indian Food Composition Tables (2017, Rev 2018), National Institute of Nutrition (ICMR-NIN), Hyderabad.
- **`USDA_FDC`**: USDA FoodData Central (SR Legacy & Foundation Foods), Agricultural Research Service.
- **`OPEN_FOOD_FACTS`**: Open Food Facts India Packaged Database (ODbL 1.0).
- **`FITKIT_RECIPE_ENGINE`**: FitKit Deterministic Recipe Simulation Engine for cooked composite dishes.

### Standardized Attributes
For each food item:
- `foodCode`: Unique canonical code (e.g., `IFCT_A018`, `USDA_168878`).
- `source`: Foreign key to `source_provenance`.
- `sourceRecordId`: Specific accession ID within the source dataset.
- `name`: Common and regional culinary name.
- `scientificName`: Binomial biological classification.
- `foodGroup`: Culinary/botanical category (e.g. Cereal Grains, Edible Oils, Vegetables).
- `foodState`: Explicit state classification (`RAW_INGREDIENT`, `COOKED_SINGLE`, `PREPARED_RECIPE`, `PACKAGED_COMMERCIAL`).
- `qualityStatus`: `SOURCE_VERIFIED`, `CALCULATED_ESTIMATE`, or `CORRECTED_ANOMALY`.
- `isVerified`: Boolean confirmation of scientific review.

---

## 3. Catalog Quality Audit

### 3.1 Existing Catalog Audit (26 Foods)

| ID | Food Code | Name | State | Energy (kcal) | P (g) | C (g) | F (g) | Fib (g) | Quality Status | Findings & Audit Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `USDA_168878` | Rice, white, long-grain, raw | RAW | 365.0 | 6.67 | 79.95 | 0.66 | 1.30 | VERIFIED | Standard raw grain baseline. Moisture: 11.62%. |
| 2 | `USDA_2345388` | Rice, white, cooked, regular | COOKED | 130.0 | 2.69 | 28.17 | 0.28 | 0.40 | VERIFIED | Hydration expansion yield ≈ 2.80x. Moisture: 68.44%. |
| 3 | `IFCT_B021` | Red gram, dal (Toor dal), raw | RAW | 330.8 | 21.70 | 55.23 | 1.56 | 9.06 | VERIFIED | Converted from 1384 kJ (1384 / 4.184 = 330.8 kcal). |
| 4 | `IFCT_T013` | Ghee (Clarified Butter) | RAW | 900.0 | 0.00 | 0.00 | 100.00 | 0.00 | CORRECTED | Source reported 0 kJ anomaly. Corrected via 9.0 kcal/g lipid standard. |
| 5 | `IFCT_L003` | Paneer (Fresh Cottage Cheese) | RAW | 265.0 | 18.30 | 2.10 | 20.80 | 0.00 | VERIFIED | Moisture: 54.1%. High protein dairy baseline. |
| 6 | `OFF_8901262010014` | Amul Pasteurized Butter | PACKAGED | 717.0 | 0.50 | 0.00 | 80.00 | 0.00 | VERIFIED | Commercial salted table butter. |
| 7 | `IFCT_A019` | Wheat flour, whole (Atta) | RAW | 320.3 | 10.57 | 64.21 | 1.48 | 11.23 | VERIFIED | Converted from 1340 kJ (1340 / 4.184 = 320.3 kcal). |
| 8 | `IFCT_B003` | Black gram, dal (Urad dal), raw | RAW | 324.0 | 22.80 | 54.50 | 1.40 | 11.50 | VERIFIED | High protein pulse baseline. |
| 9 | `RECIPE_CHAPATI_01` | Chapati / Phulka | RECIPE | 242.65 | 8.01 | 46.36 | 1.30 | 8.51 | ESTIMATE | Modeled from 100g atta, yield 1.32x (20% dough moisture loss). |
| 10 | `RECIPE_IDLI_01` | Steamed Idli | RECIPE | 147.81 | 4.45 | 31.38 | 0.46 | 1.72 | ESTIMATE | Modeled from 3:1 rice/urad batter, yield 2.40x. |
| 11 | `RECIPE_DAL_TADKA_01` | Dal Tadka (with Ghee) | RECIPE | 117.44 | 6.78 | 17.26 | 2.05 | 2.83 | ESTIMATE | Modeled from 100g dal + 5g ghee, yield 3.048x. |
| 12 | `IFCT_A007_COOKED` | Basmati Rice, cooked | COOKED | 121.0 | 2.54 | 26.80 | 0.38 | 0.60 | VERIFIED | Converted from 506 kJ. Moisture: 69.5%. |
| 13 | `RECIPE_DOSA_01` | Plain Sada Dosa | RECIPE | 168.0 | 3.90 | 29.50 | 3.70 | 1.40 | ESTIMATE | Modeled from fermented batter + 3g cooking oil. |
| 14 | `IFCT_SAMBAR_01` | Vegetable Sambar | RECIPE | 58.0 | 2.80 | 8.50 | 1.40 | 2.20 | VERIFIED | Converted from 243 kJ. Moisture: 84.1%. |
| 15 | `IFCT_B024` | Green gram / Moong dal, raw | RAW | 348.0 | 24.50 | 59.90 | 1.20 | 8.20 | VERIFIED | Converted from 1456 kJ. |
| 16 | `IFCT_B024_COOKED` | Moong Dal, cooked plain | COOKED | 105.0 | 6.80 | 16.50 | 1.10 | 2.40 | VERIFIED | Boiled plain dal. Hydration yield ~3.3x. |
| 17 | `IFCT_L007` | Curd / Dahi (Cow Milk) | RAW | 61.0 | 3.20 | 4.40 | 3.40 | 0.00 | VERIFIED | Converted from 255 kJ. |
| 18 | `IFCT_L001` | Cow Milk, whole fresh | RAW | 67.0 | 3.20 | 4.80 | 3.90 | 0.00 | VERIFIED | Converted from 280 kJ. |
| 19 | `USDA_173424` | Egg, whole, boiled | COOKED | 155.0 | 12.58 | 1.12 | 10.61 | 0.00 | VERIFIED | Hard-boiled whole chicken egg. |
| 20 | `USDA_171077` | Chicken breast, cooked | COOKED | 165.0 | 31.02 | 0.00 | 3.57 | 0.00 | VERIFIED | Roasted/boiled skinless meat. |
| 21 | `IFCT_ROHU_COOKED` | Rohu Fish, cooked | COOKED | 112.0 | 19.80 | 0.00 | 3.10 | 0.00 | VERIFIED | Freshwater carp. Converted from 469 kJ. |
| 22 | `IFCT_C019_COOKED` | Spinach / Palak, steamed | COOKED | 26.0 | 2.40 | 3.20 | 0.40 | 2.60 | VERIFIED | Converted from 109 kJ. Moisture: 90.2%. |
| 23 | `IFCT_E010_COOKED` | Potato (Aloo), boiled | COOKED | 87.0 | 1.80 | 19.50 | 0.10 | 1.80 | VERIFIED | Converted from 364 kJ. |
| 24 | `IFCT_F005` | Banana (Robusta / Ripe) | RAW | 89.0 | 1.10 | 22.80 | 0.30 | 2.60 | VERIFIED | Converted from 372 kJ. |
| 25 | `USDA_171688` | Apple, fresh with skin | RAW | 52.0 | 0.26 | 13.81 | 0.17 | 2.40 | VERIFIED | USDA SR Legacy fresh apple. |
| 26 | `IFCT_A011_COOKED` | Poha, cooked seasoned | RECIPE | 145.0 | 2.60 | 25.40 | 3.80 | 1.20 | VERIFIED | Flattened rice with onion & oil. Converted from 607 kJ. |

---

## 4. Distinguishing Genuine Zeros from Missing Values

A critical nutrition-tracking defect in generic software is treating missing micronutrients or unmeasured proximates as `0.0`.

### Rules Enforced in FitKit:
1. **Genuine Zeros**:
   - Pure fats (Ghee, Mustard Oil, Refined Oil) legitimately contain `0.0g` carbohydrate, `0.0g` protein, and `0.0g` dietary fiber.
   - Animal meats (Chicken Breast, Rohu Fish) legitimately contain `0.0g` dietary fiber.
   - Hard-boiled eggs contain negligible carbs (`1.12g`) and `0.0g` fiber.
2. **Missing / Unanalyzed Values**:
   - If a source does not analyze dietary fiber, ash, moisture, or specific micronutrients, the corresponding field in `food_nutrients` remains `NULL`.
   - The UI displays `--` instead of `0g` for null fields.
   - In recipe aggregation, missing nutrients trigger a transparent calculation note (e.g. `"WARNING: Ingredient [X] lacks fiber baseline"`), preventing silent undercounting.

---

## 5. Food-State Distinctions & Yield Factors

FitKit enforces clear behavioral distinctions between:
- **`RAW_INGREDIENT`**: Uncooked state used for recipe formulation (e.g. raw rice, raw dal, raw wheat flour, raw onions). Moisture content is low in dry grains/pulses (9–12%) and high in raw vegetables (85–95%).
- **`COOKED_SINGLE`**: Single ingredient cooked with water or heat (e.g. cooked Basmati rice, boiled egg, boiled potato). Densities reflect hydration or evaporation.
- **`PREPARED_RECIPE`**: Composite dishes composed of multiple raw ingredients, cooking fats, water, and seasonings.
- **`PACKAGED_COMMERCIAL`**: Branded foods with verified manufacturer labels.

### Raw vs Cooked Hydration & Moisture Principles:
- **Dry Grains & Pulses**: Expand by absorbing water ($Y = 2.4 - 3.3$). Calorie density drops from ~340 kcal/100g raw to ~120–140 kcal/100g cooked.
- **Breads / Chapatis**: Dough loses moisture during tawa baking ($Y = 1.25 - 1.35$ relative to dry flour). Calorie density is ~242 kcal/100g cooked flatbread.
- **Leafy Greens & Vegetables**: Sautéing or boiling evaporates water or leaches juice ($Y = 0.70 - 0.90$).

---

## 6. Catalog Expansions for Custom Recipe Formulation (Flyway `V6` & Reconciled `V7`)

To enable users to formulate everyday Indian custom recipes (such as Homemade Vegetable Upma, Paneer Bhurji, Vegetable Pulao, Egg Dosa, and Dal Tadka), staple raw ingredients were introduced in migration `V6` and audited/reconciled in migration `V7` against authoritative **Indian Food Composition Tables (IFCT 2017, ICMR-NIN)** records:

| Food Code | Name | State | Energy (kcal) | Energy (kJ) | P (g) | C (g) | F (g) | Fib (g) | Standard Portion | Authoritative Source Record | Quality Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `IFCT_A022` | Semolina / Suji / Rava | RAW | 333.7 | 1396.0 | 11.38 | 68.43 | 0.80 | 9.72 | 1/2 cup (90g, ~300 kcal) | IFCT 2017 A022 (*Triticum aestivum*) | `SOURCE_VERIFIED` |
| `IFCT_G017` | Onion, fresh raw | RAW | 48.5 | 203.0 | 1.20 | 9.30 | 0.40 | 2.50 | 1 medium (70g, ~34 kcal) | IFCT 2017 G017 (*Allium cepa*, big onion) | `SOURCE_VERIFIED` |
| `IFCT_D075` | Tomato, ripe raw | RAW | 20.8 | 87.0 | 0.90 | 3.60 | 0.20 | 1.70 | 1 medium (90g, ~19 kcal) | IFCT 2017 D075 (*Solanum lycopersicum*, hybrid) | `SOURCE_VERIFIED` |
| `IFCT_G011` | Green Chili, fresh raw | RAW | 45.7 | 191.0 | 2.60 | 6.80 | 0.60 | 6.80 | 1 chili (5g, ~2.3 kcal) | IFCT 2017 G011 (*Capsicum annuum*) | `SOURCE_VERIFIED` |
| `IFCT_G025` | Cumin Seeds (Jeera) | RAW | 304.5 | 1274.0 | 18.70 | 36.60 | 15.00 | 22.62 | 1 tsp (5g, ~15 kcal) | IFCT 2017 G025 (*Cuminum cyminum*) | `SOURCE_VERIFIED` |
| `IFCT_G026` | Turmeric Powder (Haldi) | RAW | 280.6 | 1174.0 | 6.30 | 64.90 | 3.20 | 21.10 | 1/2 tsp (2g, ~6 kcal) | IFCT 2017 G026 (*Curcuma longa*) | `SOURCE_VERIFIED` |
| `IFCT_T017` | Mustard Oil, cold-pressed | RAW | 884.0 | 3700.0 | 0.00 | 0.00 | 100.00 | 0.00 | 1 tsp (5g, ~44 kcal), 1 tbsp (14g) | IFCT 2017 T017 (*Brassica juncea*) | `CORRECTED_ANOMALY` |

### Reconciliation & Atwater Energy Validation Notes:
1. **Semolina (`IFCT_A022`):** Corrected food code from `IFCT_A018` (which in IFCT 2017 designates refined wheat flour / maida) to `IFCT_A022`. Stored source energy is 1396 kJ (333.7 kcal). Atwater estimate ($11.38 \times 4 + 68.43 \times 4 + 0.80 \times 9$) is 326.4 kcal.
2. **Onion (`IFCT_G017`):** Reconciled code from `IFCT_C012` to `G017`. Available carbohydrates is 9.30g per 100g (legacy V6 used 11.10g total carbohydrate by difference). Stored energy is 203 kJ (48.5 kcal). Atwater estimate is 45.6 kcal.
3. **Tomato (`IFCT_D075`):** Reconciled code from `IFCT_C035` to `D075` (ripe hybrid tomato). Stored energy is 87 kJ (20.8 kcal). Atwater estimate is 19.8 kcal.
4. **Green Chili (`IFCT_G011`):** Reconciled code from legacy `IFCT_D005` to `G011`. Available carbohydrates is 6.80g (V6 used 7.80g). Stored energy is 191 kJ (45.7 kcal). Atwater estimate is 43.0 kcal.
5. **Cumin Seeds (`IFCT_G025`):** Reconciled code from `IFCT_D004` to `G025`. Corrected energy from legacy NVIF 375.0 kcal (which included fiber in carbohydrates by difference) to authoritative IFCT 2017 source energy of 1274 kJ (304.5 kcal), with available carbohydrates 36.60g and dietary fiber 22.62g.
6. **Turmeric Powder (`IFCT_G026`):** Reconciled code from `IFCT_D028` to `G026`. Corrected energy from legacy 349.0 kcal to IFCT 2017 source energy of 1174 kJ (280.6 kcal), total fat 3.20g, and dietary fiber 21.10g.
7. **Mustard Oil (`IFCT_T017`):** The raw IFCT 2017 table printed "0 kJ" for entry T017. Stored energy is reconciled to 884.0 kcal (3700 kJ) using FAO/INFOODS specific vegetable oil energy factor (8.84 kcal/g for 100g lipid); standard 9.0 kcal/g yields 900.0 kcal (3765.6 kJ). Provenance is flagged as `CORRECTED_ANOMALY`.

---

## 7. Phase 7 Expansion: Indian Staples & Search Aliases (Flyway `V8`)

In Phase 7, the catalog was expanded from 33 to 51 food items through forward-only migration `V8__expand_food_catalog_and_search_aliases.sql`:
- **18 New IFCT 2017 Staples**: Brown Rice (A008), Rolled Oats (A015), Besan (B006), Raw Poha (A011), Chana Dal (B005), Kabuli Chana (B008), Rajma (B018), Ginger (G010), Garlic (G009), Curry Leaves (C009), Coriander Leaves (C008), Carrot (D014), Brinjal (D008), Spinach (C019), Potato (E010), Fresh Coconut Kernel (F012), Lemon Juice (F023), and Tamarind Pulp (G031).
- **Search Aliases (`food_aliases`)**: Multilingual regional synonyms and transliterations (`suji`, `rava`, `atta`, `dahi`, `pyaaz`, `tamatar`, `aloo`, `adrak`, `lahsun`, `baingan`, `kadi patta`, `chana`, `rajma`, `besan`, etc.) across English, Hindi, Tamil, Telugu, Kannada, and Gujarati.
- **Deterministic 6-Tier Relevance Ranking**: Exact canonical name -> Exact alias -> Canonical prefix -> Alias prefix -> Partial canonical -> Partial alias/fields.
- **For full verification and catalog details**, see [`docs/phase-7-implementation-report.md`](file:///d:/Projects/WebProgramming/docs/phase-7-implementation-report.md).
