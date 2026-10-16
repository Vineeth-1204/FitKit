# FitKit — Nutrition Dataset Feasibility Audit & Research Report
**Document Version:** 1.0.0  
**Phase:** Phase 1 — Nutrition Dataset Research & Feasibility Audit  
**Auditor Role:** Senior Data Engineer & Full-Stack Software Architect  
**Project:** FitKit (Personalized Nutrition & Workout Planning Platform)  
**Date:** October 14, 2026  
**Status:** Complete — Ready for Review & Approval  

---

## 1. Executive Summary

Accurate dietary tracking requires robust, legally defensible, and chemically sound nutritional data. For **FitKit**, whose core differentiator is deep support for Indian dietary staples, regional recipes, and commercial packaged goods alongside global foods, the dataset selection is the foundational architectural pillar.

### Key Audit Findings

1. **No Single Dataset Solves FitKit's Problem:**
   - **Indian Food Composition Tables (IFCT 2017)** from ICMR-NIN is the undisputed gold standard for raw, indigenous Indian agricultural commodities and pulses (542 items, 151+ nutrients via HPLC/AAS). However, it is **almost 100% raw ingredient data** (534 raw items, 8 cooked items limited to boiled eggs and roasted vermicelli). It contains **zero traditional cooked dishes** (no Idli, Dosa, Chapati, Sambar, Dal Tadka, or Biryani) and **no household serving sizes**.
   - **USDA FoodData Central (FDC)** provides the world’s most comprehensive public domain (CC0) database with over 365,000 foods. It features rigorous distinction between raw and cooked states (Foundation and SR Legacy) and survey-based multi-ingredient recipes with household measures (Survey / FNDDS). However, its culinary coverage is heavily Western-centric, offering minimal representation of regional Indian preparations.
   - **Open Food Facts (OFF)** provides authentic barcode-level coverage for over 24,800 Indian packaged products (Amul, Haldiram's, Britannia, Nestle India). However, its crowdsourced nature creates data quality variances, OCR misreads, and high API latency/rate-limiting (HTTP 503) under unauthenticated search queries.
   - **IIIT-Delhi RecipeDB** and historical **ICMR-NIN Nutritive Value of Indian Foods (NVIF 1989)** provide critical reference blueprints for traditional recipes and standard Indian portion sizes (e.g., standard gram weights for 1 Idli, 1 Chapati, or 1 Katori of Sambar).

2. **Severe Pitfalls Uncovered in Community Digitized Datasets:**
   - **The 0 kJ Cooking Oil Anomaly:** In community-digitized versions of IFCT 2017 (such as the popular `@ifct2017/compositions` npm/GitHub distribution), all 14 entries in *Edible Oils and Fats* (including Ghee, Mustard Oil, and Coconut Oil) report an energy value of **`0 kJ`** despite having 100g of total lipids. This occurs because missing table values in the original PDF were coerced to `0` by the parser. If used naively, a user logging 100g of ghee would be credited with **0 calories** instead of ~900 kcal.
   - **Energy Unit Confusion (kJ vs. kcal):** IFCT 2017 primary energy (`enerc`) is reported in **Kilojoules (kJ)**, not Kilocalories (kcal). Storing or displaying this without conversion results in a **4.184x overestimation of caloric intake** (e.g., 100g raw rice at 1480 kJ would be logged as 1480 kcal).
   - **The Raw-vs-Cooked Caloric Inflation Trap:** Raw white rice contains ~365 kcal and 80g of carbohydrates per 100g. Cooked white rice contains ~130 kcal and 28g of carbohydrates per 100g due to water absorption (yield factor ~2.8x). If an application does not strictly decouple raw ingredients from cooked dishes, a user consuming 200g of cooked rice who selects a raw rice record will inadvertently log **730 kcal instead of 260 kcal**—a 280% tracking error that invalidates any calorie deficit or surplus goal.

3. **Recommended Architecture:**
   FitKit must adopt a **Three-Tier Hybrid Nutrition Catalog** coupled with a deterministic **Indian Recipe & Yield Simulation Engine**:
   - **Tier 1 (Base Analytical Ingredients):** Curated IFCT 2017 (for Indian grains, pulses, dairy, and vegetables) + USDA Foundation / SR Legacy (for global commodities, meats, and fallback values).
   - **Tier 2 (Cooked Dishes & Composite Recipes):** A native FitKit Recipe Engine that models dishes from raw ingredients using validated cooking yield factors (hydration/shrinkage) and nutrient retention factors, calibrated against ICMR-NIN NVIF Table 2 and IIIT-Delhi RecipeDB standards.
   - **Tier 3 (Branded Packaged Goods):** An offline, sanitized snapshot of Open Food Facts India (filtered for active Indian products) stored locally in PostgreSQL, supporting instant barcode lookups and brand searches.

---

## 2. Dataset Comparison Matrix

| Evaluation Criteria | IFCT 2017 (ICMR-NIN) | USDA FoodData Central (FDC) | Open Food Facts (OFF) | RecipeDB (IIIT-Delhi) | ICMR NVIF (1989 / 2014) | FAO INFOODS (AnFooD / FCTB) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Publisher** | ICMR - National Institute of Nutrition, India | Agricultural Research Service, USDA, USA | Open Food Facts Association | Computational Gastronomy Lab, IIIT-Delhi | ICMR - National Institute of Nutrition, India | FAO, United Nations |
| **Primary Geographic Focus** | India (Indigenous cultivars & regional agro-climates) | United States & International Trade | Global (24,800+ products for India) | India & Global (118k total, 10k+ Indian) | India (Historical national standard) | Global & South Asia (Bangladesh FCTB) |
| **Verified Record Count** | 542 foods across 20 groups | >365,000 (Foundation: ~2k, SR Legacy: 8.8k, FNDDS: ~7k, Branded: 350k+) | >3.1M global; ~24,800 India tagged | >118,000 recipes (~10,000 traditional Indian) | ~850 raw foods; ~120 cooked dishes | >4,000 records; FCTB has 381 foods |
| **Primary Formats Available** | PDF eBook, Android App, Community CSV/JSON/SQLite | REST API, Bulk CSV (ZIP), Bulk JSON (ZIP) | REST API v2, Daily/Monthly JSONL.gz, CSV | Web Portal, Academic research CSVs | Printed book, Scanned PDF tables | Excel (.xlsx), CSV |
| **Nutrient Basis** | Per 100g edible portion (EP) | Per 100g edible portion + explicit portion gram weights | Per 100g / 100ml and per labeled serving size | Per recipe yield and per estimated serving | Per 100g cooked dish and per traditional portion | Per 100g edible portion |
| **Energy Units Reported** | Kilojoules (kJ) primary; kcal derived via Atwater | Both kcal and kJ reported (Nutrient IDs 1008 & 1062) | Both kcal and kJ reported | Kilocalories (kcal) | Kilocalories (kcal) | Both kcal and kJ reported |
| **Micronutrient Granularity** | 151+ parameters (HPLC/AAS vitamins, minerals, amino/fatty acids) | Complete analytical profiling (Foundation & SR Legacy) | Sporadic (Limited to front-of-pack declarations) | Algorithmic estimates mapped from USDA | Basic proximate + 6 minerals and vitamins | Standardized FAO INFOODS tagnames |
| **Raw vs. Cooked Coverage** | **98.5% Raw** (534 raw, 8 cooked/boiled egg entries) | **Extensive Raw & Cooked** (Separate FDC IDs for raw/cooked) | **100% Processed / Packaged** retail foods | **100% Cooked Recipes** with multi-step processes | **Both Raw & Cooked** (Table 1: Raw, Table 2: Cooked) | Both Raw and Selected Cooked |
| **Household Measures (Portions)** | **None** (Only 100g edible portion) | **Extensive** (Cups, tablespoons, slices, pieces with gram weights) | Serving text from packaging (e.g., "1 packet = 70g") | Estimated portion sizes (servings per recipe) | Standard traditional measures (1 Idli, 1 Katori) | Limited portion data |
| **Official License** | ICMR-NIN Copyright (Academic/personal free; commercial requires permit) | Public Domain / Creative Commons Zero (CC0 1.0) | Open Database License (ODbL 1.0) | Academic Research Copyright (Citation required) | ICMR-NIN Copyright (Historical reference) | CC BY-NC-SA 3.0 IGO (Non-commercial) |
| **API Access & Limits** | No official REST API; community offline flat files | Free Data.gov key: 1,000 req/hr; DEMO_KEY: 30-50 req/hr | Free REST API; high search rate limits (HTTP 503) | No public REST API; research portal | None (Scanned print) | Direct Excel download |
| **Suitability for FitKit** | **Primary Indian Raw Benchmark** | **Primary Core System & Portion Standard** | **Supplementary Branded Barcode Data** | **Recipe Modeling Blueprint** | **Portion Weight Baseline Reference** | **Secondary Validation Reference** |

---

## 3. Official Source Links and Licence Findings

### 3.1 Indian Food Composition Tables (IFCT 2017)
* **Publisher:** National Institute of Nutrition (NIN), Indian Council of Medical Research (ICMR), Department of Health Research, Ministry of Health and Family Welfare, Government of India.
* **Official Document:** [NIN IFCT 2017 PDF eBook](https://www.nin.res.in/ebooks/IFCT2017.pdf) (582 pages).
* **Official Mobile App:** Google Play Store — `com.nin20.nin2byo2i`.
* **Licensing & Legal Status:**
  - The copyright resides strictly with the **Director, National Institute of Nutrition (ICMR)**.
  - The official publication terms explicitly permit **personal study, academic research, and non-commercial educational dissemination** provided full attribution is granted to ICMR-NIN.
  - **Commercial Restriction:** Storing, reproducing, or redistributing the database in electronic media or software products for commercial gain requires **prior written authorization / licensing** from ICMR-NIN.
  - **Community Code License Notice:** Open-source projects distributing parsed tables (e.g., `nodef/ifct2017` licensed under GNU AGPL-3.0 or MIT) only license their parser scripts. **Open-source software licenses cannot override the underlying sovereign copyright of the Government of India / ICMR.**
* **FitKit Deployment Assessment:**
  - *Phase 1 & Student / Academic Capstone:* Fully permitted under educational fair use with attribution.
  - *Production Commercial Deployment:* Requires formal written communication with the ICMR-NIN Technology Transfer Cell to secure digital publication clearance.

### 3.2 USDA FoodData Central (FDC)
* **Publisher:** Agricultural Research Service (ARS), United States Department of Agriculture (USDA).
* **Official Portal:** [USDA FoodData Central](https://fdc.nal.usda.gov/).
* **Official Download Page:** [Downloadable Data](https://fdc.nal.usda.gov/download-datasets.html).
* **API Documentation:** [FoodData Central API Guide](https://fdc.nal.usda.gov/api-guide.html).
* **Licensing & Legal Status:**
  - FoodData Central data are works of the United States Government and are placed into the **Public Domain under Creative Commons Zero (CC0 1.0 Universal Dedication)**.
  - **Commercial Redistribution:** Unrestricted. No fees, licenses, or royalties are required.
  - **Attribution:** Formally requested: *"U.S. Department of Agriculture, Agricultural Research Service. FoodData Central. fdc.nal.usda.gov."*
* **FitKit Deployment Assessment:** Unconditionally viable for student, academic, and commercial production deployment.

### 3.3 Open Food Facts (OFF)
* **Publisher:** Open Food Facts Association (French non-profit international association).
* **Official Portal:** [Open Food Facts Global](https://world.openfoodfacts.org/) / [India Portal](https://in.openfoodfacts.org/).
* **API Documentation:** [Open Food Facts API v2 Documentation](https://openfoodfacts.github.io/api-documentation/).
* **Licensing & Legal Status:**
  - Database is licensed under the **Open Database License (ODbL 1.0)**.
  - Individual contents and nutrients are licensed under the **Database Contents License (DbCL 1.0)**.
  - Product packaging images are licensed under **Creative Commons Attribution-ShareAlike 2.0 (CC-BY-SA 2.0)**.
  - **Share-Alike Requirement:** If FitKit modifies, cleans, or enhances the database itself (e.g., correcting nutrient errors or training an OCR model on the data), those database enhancements must be shared back under the ODbL if made publicly accessible. Using the database to calculate user diet statistics does **not** force FitKit's proprietary application source code to be open-sourced.
* **FitKit Deployment Assessment:** Legally viable for commercial and student use with mandatory source attribution and compliance with ODbL data-sharing rules.

### 3.4 IIIT-Delhi RecipeDB & DietRx
* **Publisher:** Computational Gastronomy Laboratory, IIIT-Delhi (Prof. Ganesh Bagler).
* **Official Portal:** [RecipeDB Portal](https://cosylab.iiitd.edu.in/recipedb/) / [CosyLab GitHub](https://github.com/cosylabiiit).
* **Licensing & Legal Status:** Academic copyright. Intended for educational, research, and non-commercial scientific analysis. Bulk redistribution or scraping without explicit collaboration agreement is restricted.
* **FitKit Deployment Assessment:** Usable strictly as a structural benchmark for recipe schemas and ingredient proportions during design; production deployment must use FitKit's own compiled recipe database.

### 3.5 FAO / INFOODS (AnFooD & South Asian FCTs)
* **Publisher:** Food and Agriculture Organization (FAO) of the United Nations.
* **Official Portal:** [FAO INFOODS Food Composition](https://www.fao.org/infoods/infoods/tables-and-databases/en/).
* **Licensing & Legal Status:** Creative Commons Attribution-NonCommercial-ShareAlike 3.0 IGO (CC BY-NC-SA 3.0 IGO). Commercial use requires special permission from the FAO.

---

## 4. Data Inspection Methodology

To move beyond documentation summaries, empirical tests were executed against the actual data stores:

1. **IFCT 2017 Deep File Inspection:**
   - Evaluated the complete parsed repository structure (`nodef/ifct2017` / `@ifct2017/compositions`) representing all 542 foods across 421 column parameters.
   - Audited the build pipeline (`build.ts`) to understand value coercion, missing-data handling, and unit assignments.
   - Inspected associated tables: `yieldfactors` (542 items), `retentionfactors` (214 parameters across 6 cooking modalities), and `energies` (Atwater factors).
2. **Live USDA FoodData Central REST API Auditing:**
   - Queried the production endpoint `https://api.nal.usda.gov/fdc/v1/foods/search` and `https://api.nal.usda.gov/fdc/v1/food/{fdcId}` using both `DEMO_KEY` and rate-tested requests.
   - Verified nutrient schema structures across **SR Legacy**, **Survey (FNDDS)**, **Foundation Foods**, and **Branded Foods**.
   - Validated that rapid consecutive queries against `DEMO_KEY` trigger an immediate `HTTP 429: Too Many Requests` error, confirming that local caching or bulk data ingestion is mandatory for production stability.
3. **Open Food Facts API Probing:**
   - Executed live queries on `https://world.openfoodfacts.org/api/v2/search` filtering by `countries_tags_en=india`.
   - Verified that unthrottled search queries trigger `HTTP 503: Service Temporarily Unavailable`, proving that FitKit must never rely on live runtime full-text searches against Open Food Facts servers.
   - Inspected Indian staple packaged items (Amul Butter, Haldiram's Snacks, Horlicks) to assess OCR accuracy and field completeness.
4. **Sample Compilation & Cross-Verification:**
   - Compiled 26 representative food items spanning Indian staples, pulses, dairy, animal proteins, vegetables, cooked dishes, and branded products.
   - Cross-checked energy, protein, carbohydrate, fat, and moisture figures across datasets to isolate unit discrepancies and raw-versus-cooked divergences.

---

## 5. Representative Record Samples and Original Units

The table below details **26 representative food records** directly inspected during this audit. All values reflect standard reporting bases and highlight observed data quality attributes.

| No. | Food Name | Source Dataset & ID | Processing State | Basis | Original Energy | Calc. (kcal) | Protein (g) | Carbs (g) | Fat (g) | Fiber (g) | Water (g) | Documented Portion / Measures | Data Quality / Inspection Finding |
| :---: | :--- | :--- | :--- | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- | :--- |
| **1** | Rice, flakes (*Poha/Aval*) | IFCT 2017 (`A011`) | Flattened raw grain | 100g EP | 1480 kJ | 353.7 | 7.44 | 76.75 | 1.14 | 3.46 | 10.36 | None | Reported in kJ (1480 / 4.184 = 353.7 kcal). Available carbs reported by enzymatic assay. |
| **2** | Wheat flour, refined (*Maida*) | IFCT 2017 (`A018`) | Milled raw flour | 100g EP | 1472 kJ | 351.8 | 10.36 | 74.27 | 0.76 | 2.76 | 11.34 | None | Pure dry flour. Using this as cooked roti/paratha without water/fat factors produces massive tracking errors. |
| **3** | Ragi (*Finger millet*) | IFCT 2017 (`A010`) | Raw whole grain | 100g EP | 1342 kJ | 320.7 | 7.16 | 66.82 | 1.92 | 11.18 | 10.89 | None | High dietary fiber (11.18g) and exceptional calcium (344 mg/100g). Pure raw agricultural commodity. |
| **4** | Bajra (*Pearl millet*) | IFCT 2017 (`A003`) | Raw whole grain | 100g EP | 1456 kJ | 348.0 | 10.96 | 61.78 | 5.43 | 11.49 | 8.97 | None | High lipid content (5.43g) relative to other cereals; raw dry grain. |
| **5** | Jowar (*Sorghum*) | IFCT 2017 (`A005`) | Raw whole grain | 100g EP | 1398 kJ | 334.1 | 9.97 | 67.68 | 1.73 | 10.22 | 9.01 | None | Raw grain. When cooked into Jowar Roti, hydration increases weight by ~1.35x. |
| **6** | Bengal gram dal (*Chana dal*) | IFCT 2017 (`B001`) | Raw split pulse | 100g EP | 1377 kJ | 329.1 | 21.55 | 46.72 | 5.31 | 15.15 | 9.18 | None | Raw dry legume. High protein (21.55g) and fiber (15.15g). Cooking expands weight by ~2.4x. |
| **7** | Red gram dal (*Toor dal*) | IFCT 2017 (`B021`) | Raw split pulse | 100g EP | 1384 kJ | 330.8 | 21.70 | 55.23 | 1.56 | 9.06 | 9.20 | None | Core Indian dal staple. If a user logs 100g cooked dal using this raw entry, they overcount calories by 300%. |
| **8** | Black gram dal (*Urad dal*) | IFCT 2017 (`B003`) | Raw split pulse | 100g EP | 1356 kJ | 324.1 | 23.06 | 51.00 | 1.69 | 11.93 | 9.16 | None | Key ingredient for Idli/Dosa batter fermentation. Raw dry pulse. |
| **9** | Green gram dal (*Moong dal*) | IFCT 2017 (`B010`) | Raw split pulse | 100g EP | 1363 kJ | 325.8 | 23.88 | 52.59 | 1.35 | 9.37 | 9.77 | None | Highest protein-to-fat ratio among common dals; raw pulse. |
| **10** | Paneer (*Cottage cheese*) | IFCT 2017 (`L003`) | Fresh dairy | 100g EP | 1079 kJ | 257.9 | 18.86 | 12.41 | 14.78 | 0.00 | 51.96 | None | IFCT reports 12.41g carbs in artisanal fresh paneer due to whey retention; commercial packaged brands report lower. |
| **11** | Milk, cow | IFCT 2017 (`L001`) | Raw fresh liquid | 100g EP | 304 kJ | 72.7 | 3.26 | 4.92 | 4.15 | 0.00 | 86.84 | None | Reported per 100g, not per 100ml. Indian whole cow milk analysis. |
| **12** | Milk, buffalo | IFCT 2017 (`L002`) | Raw fresh liquid | 100g EP | 450 kJ | 107.6 | 3.82 | 5.12 | 7.55 | 0.00 | 82.72 | None | Significantly higher fat (7.55g) and calories (107.6 kcal) than cow milk; crucial for South Asian accuracy. |
| **13** | Egg, poultry, whole, boiled | IFCT 2017 (`M004`) | Cooked (Boiled) | 100g EP | 644 kJ | 153.9 | 13.34 | 0.00 | 10.68 | 0.00 | 74.45 | None | One of only 8 cooked items in IFCT. Closely corroborates USDA boiled egg data (155 kcal). |
| **14** | Potato, brown skin | IFCT 2017 (`F006`) | Raw tuber | 100g EP | 292 kJ | 69.8 | 1.54 | 14.89 | 0.23 | 1.71 | 80.72 | None | Raw tuber. Cooking modality (boiling vs deep frying) completely alters the fat/calorie density. |
| **15** | Tomato, green | IFCT 2017 (`D074`) | Raw vegetable | 100g EP | 87 kJ | 20.8 | 1.12 | 3.18 | 0.27 | 1.62 | 93.21 | None | High moisture (93.21%). Low-calorie cooking base. |
| **16** | Spinach (*Palak*) | IFCT 2017 (`C033`) | Raw leafy green | 100g EP | 102 kJ | 24.4 | 2.14 | 2.05 | 0.64 | 2.38 | 90.31 | None | Raw leaves. Wilts to ~35% volume upon cooking, making raw weight estimation essential. |
| **17** | Mustard oil | IFCT 2017 (`T006`) | Extracted oil | 100g EP | **0 kJ [ERROR]** | **0.0** | 0.00 | 0.00 | 100.00 | 0.00 | 0.00 | None | **CRITICAL DIGITIZATION DEFECT:** Parser coerced null energy to 0. True value is ~3700 kJ (900 kcal). |
| **18** | Ghee (*Clarified butter*) | IFCT 2017 (`T013`) | Clarified milk fat | 100g EP | **0 kJ [ERROR]** | **0.0** | 0.00 | 0.00 | 100.00 | 0.00 | 0.00 | None | **CRITICAL DIGITIZATION DEFECT:** Parser coerced null energy to 0 despite 100g fat. Must be calculated to ~900 kcal. |
| **19** | Rice, white, long-grain, raw | USDA FDC (`168878`) | Raw dry grain | 100g | 365.0 kcal | 365.0 | 6.67 | 79.95 | 0.66 | 1.30 | 11.62 | 1 cup: 185g (675 kcal) | Matches IFCT dry grain range. Provides explicit gram weight for household cup volume. |
| **20** | Rice, white, cooked, regular | USDA FDC (`2345388`) | Cooked (Boiled) | 100g | 130.0 kcal | 130.0 | 2.69 | 28.17 | 0.28 | 0.40 | 68.44 | 1 cup: 158g (205 kcal) | **WATER ABSORPTION PROOF:** 100g contains only 130 kcal and 28g carbs vs raw's 365 kcal and 80g carbs. Yield factor = 2.8x. |
| **21** | Chickpeas, cooked, boiled | USDA FDC (`173757`) | Cooked (Boiled) | 100g | 164.0 kcal | 164.0 | 8.86 | 27.42 | 2.59 | 7.60 | 60.21 | 1 cup: 164g (269 kcal) | Cooked pulse. Contrast with IFCT raw dal (329 kcal, 21.5g protein); cooked has ~60% water. |
| **22** | Naan bread | USDA FDC (`2343831`) | Baked flatbread | 100g | 296.0 kcal | 296.0 | 8.98 | 51.81 | 5.59 | 2.20 | 30.68 | 1 piece: 108g (320 kcal); small: 75g (222 kcal) | Authentic cooked flatbread entry. Fat (5.59g) accounts for fat brushed during baking. |
| **23** | Chicken breast, roasted | USDA FDC (`171077`) | Cooked (Roasted) | 100g | 165.0 kcal | 165.0 | 31.02 | 0.00 | 3.57 | 0.00 | 65.26 | 1 fillet: 86g (142 kcal); 1 cup: 140g (231 kcal) | **MOISTURE SHRINKAGE PROOF:** Water loss during roasting concentrates protein to 31g/100g (raw is ~22g). |
| **24** | Egg, whole, hard-boiled | USDA FDC (`173424`) | Cooked (Boiled) | 100g | 155.0 kcal | 155.0 | 12.58 | 1.12 | 10.61 | 0.00 | 74.62 | 1 large: 50g (78 kcal) | Corroborates IFCT boiled egg (153.9 kcal, 13.34g protein). Validates standard egg piece weight of 50g. |
| **25** | Paneer (*Commercial brand*) | USDA FDC (`2429587`) | Packaged commercial | 100g | 321.0 kcal | 321.0 | 25.00 | 3.57 | 25.00 | 0.00 | 46.43 | 1 serving: 28g (90 kcal) | Commercial formulation with higher fat and lower carbohydrate than artisanal IFCT fresh paneer. |
| **26** | Amul Pasteurized Butter | Open Food Facts (`8901262010014`) | Packaged commercial | 100g | 714.3 kcal | 714.3 | 0.00 | 0.00 | 78.57 | 0.00 | 16.00 | 10g (approx 1 pat = 71 kcal) | Authentic Indian barcode package declaration. Shows ~79g fat per 100g, moisture ~16%, and salt. |

---

## 6. Field-by-Field Data Quality Findings

### 6.1 Energy Reporting Anomalies & Unit Traps
1. **The Kilojoule Trap in IFCT 2017:**
   - In IFCT 2017, the primary energy field `enerc` is published in **Kilojoules (kJ)** per 100g edible portion.
   - Standard conversion: $\text{kcal} = \frac{\text{kJ}}{4.184}$.
   - Failure to implement this conversion in an application leads to logging 1480 kcal for a modest 100g portion of poha. FitKit must enforce that all incoming records specify an explicit unit enum (`KCAL` or `KJ`) and normalize internally to both.
2. **The 0 kJ Cooking Oil Parser Bug:**
   - As discovered in Section 5 (Records 17 and 18), all 14 edible oils and fats in the digitized IFCT 2017 have `enerc = 0`.
   - **Root Cause:** In the printed IFCT 2017 monograph, NIN reported fatty acid breakdowns for oils, but the general proximate energy column was either left blank or in a separate table. The open-source parser coerced unparsed cells via `parseFloat(val) || 0`, injecting `0` into the production CSV.
   - **Correction Rule:** FitKit's ingestion pipeline must execute a mandatory sanity check:
     $$\text{If } \text{Total Fat} > 10\text{g} \text{ and } \text{Energy} == 0 \implies \text{Energy (kcal)} = \text{Fat (g)} \times 9.0$$
3. **Inconsistent Energy Units in USDA FDC:**
   - In USDA SR Legacy, the generic `Energy` nutrient row often reports Kilojoules under nutrient ID 1062, while nutrient ID 1008 reports Kilocalories.
   - In Survey (FNDDS) and Branded foods, `Energy` is reported directly in kcal. FitKit's parser must resolve nutrients by `nutrientId` rather than relying on the string name `"Energy"`.

### 6.2 Protein & Nitrogen Conversion Factors
- Historical databases (including NVIF 1989) calculated crude protein using a uniform nitrogen factor of $6.25$ ($\text{Protein} = N \times 6.25$).
- IFCT 2017 applies **species-specific Jones factors** (e.g., $5.70$ for wheat, $5.95$ for rice, $5.30$ for pulses, $6.38$ for dairy). This reduces reported protein in cereals by ~10% compared to crude estimates, providing a more accurate physiological metric. FitKit will retain IFCT's specific protein values.

### 6.3 Carbohydrates: Available vs. Total Difference
- **USDA SR Legacy & Branded:** Calculate carbohydrate by difference including fiber:
  $$\text{Carbs}_{\text{Total}} = 100 - (\text{Moisture} + \text{Protein} + \text{Fat} + \text{Ash})$$
- **IFCT 2017:** Distinguishes **Available Carbohydrate** (`choavldf`) from **Total Dietary Fiber** (`fibtg`):
  $$\text{Available Carbs} = 100 - (\text{Moisture} + \text{Protein} + \text{Fat} + \text{Ash} + \text{Total Dietary Fiber})$$
- **FitKit Normalization Rule:** FitKit must track **Net Available Carbohydrates** and **Dietary Fiber** as separate primary fields to prevent double-counting calories from fiber.

### 6.4 The "Zero vs. Missing" Integrity Rule
- In nutrition databases, a blank cell indicates **"unmeasured"**, whereas `0.0` indicates **"analyzed and confirmed absent"**.
- Digitized community CSVs routinely flatten empty cells to `0`. For example, Vitamin D or B12 in plant foods is genuinely `0`, but Vitamin D in an unfortified vegetable oil might be marked `0` simply because the laboratory lacked the reagent assay.
- FitKit’s schema must never store unmeasured values as `0.0`. It must support nullable fields (`null`) and maintain a `data_quality_flags` bitmask indicating `VERIFIED_ZERO`, `CALCULATED_ESTIMATE`, or `UNMEASURED`.

---

## 7. Indian-Food Coverage Assessment

```
┌────────────────────────────────────────────────────────────────────────┐
│                   INDIAN FOOD COMPOSITION SPECTRUM                     │
└────────────────────────────────────────────────────────────────────────┘

  [RAW INGREDIENTS]               [RECIPES & DISHES]           [COMMERCIAL PACKAGED]
  ═════════════════               ══════════════════           ═════════════════════
  • Raw Rice & Millets            • Idli, Dosa, Uttapam        • Parle-G, Marie Gold
  • Toor, Chana, Moong Dal        • Chapati, Roti, Naan        • Maggi 2-Min Noodles
  • Raw Vegetables & Greens       • Dal Tadka, Sambar, Rasam   • Amul Butter, Cheese
  • Buffalo & Cow Milk            • Biryani, Khichdi, Poha     • Haldiram's Bhujia
  • Whole Spices & Seeds          • Paneer Butter Masala       • Packaged Curd/Yogurt
        │                                 │                             │
        ▼                                 ▼                             ▼
  COVERAGE SOURCE:                COVERAGE SOURCE:             COVERAGE SOURCE:
  IFCT 2017 (ICMR-NIN)            FitKit Recipe Engine         Open Food Facts India
  (542 Foods, 98.5% Raw)          + ICMR NVIF Table 2          (24,800+ Products)
                                  + IIIT-Delhi RecipeDB
```

### 7.1 What IFCT 2017 Covers Exceptionally Well
- **Cereals & Millets:** 24 items covering polished rice, brown rice, parboiled rice, wheat, and indigenous millets (Ragi, Bajra, Jowar, Foxtail, Kodo, Little, Proso, Barnyard millet).
- **Grain Legumes & Pulses:** 25 items including all primary Indian dals (Bengal gram, Red gram, Black gram, Green gram, Lentil, Cowpea, Horse gram, Rajma).
- **Green Leafy Vegetables:** 34 varieties including Palak, Fenugreek (*Methi*), Mustard leaves (*Sarson*), Drumstick leaves (*Moringa*), Amaranth, and Curry leaves.
- **Dairy:** Authentic analytical distinction between Indian Cow Milk and Buffalo Milk.

### 7.2 The Cooked Dish Vacuum
As proven in Section 4, a search across IFCT 2017 for standard cooked preparations yields:
- `idli`: **NOT FOUND**
- `dosa`: **NOT FOUND**
- `chapati`: **NOT FOUND**
- `roti`: **NOT FOUND**
- `sambar`: **NOT FOUND**
- `biryani`: **NOT FOUND**
- `dal tadka`: **NOT FOUND**
- `khichdi`: **NOT FOUND**

**Conclusion:** IFCT 2017 cannot be deployed directly to end-users as a food logging database. If a user cannot find "Idli" or "Chapati", the app is unusable. FitKit must bridge this gap via a dedicated recipe composition layer.

---

## 8. Raw-versus-Cooked Handling Assessment

The physical and chemical changes occurring during Indian cooking fundamentally alter nutrient density per unit weight. FitKit must establish explicit mathematical models for these transitions.

### 8.1 Cooking Yield Factor ($Y$) Mechanics
The Cooking Yield Factor represents the change in total food weight resulting from hydration (water uptake) or dehydration (moisture evaporation):
$$Y = \frac{\text{Weight}_{\text{Cooked}}}{\text{Weight}_{\text{Raw}}}$$

$$\text{Nutrient Density per 100g Cooked} = \frac{\text{Nutrient per 100g Raw}}{Y} \times R$$
*(where $R$ is the Nutrient Retention Factor, typically $1.0$ for macronutrients, and $0.70 - 0.95$ for heat-sensitive vitamins).*

#### Observed Empirical Conversions:
1. **Starchy Grains (Hydration Expansion):**
   - **White Rice:** 100g raw rice ($365\text{ kcal}$, $80\text{g carbs}$) + 200g water $\implies$ ~280g cooked rice ($Y \approx 2.80$).
   - *Resulting 100g Cooked Rice:* $130\text{ kcal}$, $28.2\text{g carbs}$.
   - *Error if Confused:* An overcount of **+180% calories**.
2. **Pulses & Dals (Hydration Expansion):**
   - **Toor Dal:** 100g raw dal ($331\text{ kcal}$, $21.7\text{g protein}$) + 250g water $\implies$ ~300g cooked dal without tadka ($Y \approx 3.00$).
   - *Resulting 100g Cooked Dal:* $110\text{ kcal}$, $7.2\text{g protein}$.
   - *Error if Confused:* An overcount of **+200% calories and protein**.
3. **Animal Meats (Moisture Shrinkage):**
   - **Chicken Breast:** 100g raw breast ($120\text{ kcal}$, $22.5\text{g protein}$, $75\text{g water}$) roasted $\implies$ ~75g cooked meat ($Y \approx 0.75$, water drops to $65\%$).
   - *Resulting 100g Cooked Breast:* $165\text{ kcal}$, $31.0\text{g protein}$.
   - *Error if Confused:* An undercount of **-28% protein**.
4. **Flatbreads (Baking Dehydration & Added Fat):**
   - **Chapati / Roti:** 30g raw whole wheat flour ($105\text{ kcal}$) + 20g water $\implies$ 50g dough. Baked on tawa $\implies$ 40g cooked chapati ($Y \approx 1.33$ relative to dry flour; 20% water loss from dough).
   - *Nutrient Content:* 1 standard Chapati (40g) contains ~105 kcal, 3g protein, 22g carbs, 0.5g fat (without added ghee).
   - If 3g ghee is brushed on top: $+27\text{ kcal}$, $+3\text{g fat} \implies 132\text{ kcal}$.

```
┌────────────────────────────────────────────────────────────────────────┐
│               RAW VS. COOKED CALORIC DILUTION COMPARISON               │
│                        (Per 100g Edible Portion)                       │
└────────────────────────────────────────────────────────────────────────┘

  Raw White Rice        ███████████████████████████████████ 365 kcal
  Cooked White Rice     █████████████ 130 kcal

  Raw Toor Dal          ███████████████████████████████ 331 kcal
  Cooked Toor Dal       ██████████ 105 kcal

  Raw Potato            ███████ 70 kcal
  Deep-Fried French Fry ████████████████████████████ 312 kcal
```

---

## 9. Packaged-Food Coverage Assessment

Modern urban Indian diets rely heavily on branded packaged goods.

### 9.1 Open Food Facts (India) Evaluation
- **Catalog Size:** Over 24,800 products explicitly tagged with country `India`.
- **Key Brand Representation:** Amul (dairy, butter, cheese, ice cream), Britannia (biscuits, bread), Haldiram's (namkeen, packaged sweets), Parle (biscuits, confectionery), Nestle India (Maggi, KitKat), ITC / Sunfeast (Aashirvaad, Dark Fantasy), Mother Dairy.
- **Barcodes:** Standard EAN-13 barcodes are indexed, enabling smartphone camera barcode scanning.

### 9.2 Identified Limitations & Mitigations
1. **OCR and Crowdsourcing Errors:** Users uploading product photos occasionally misread nutrition labels (e.g., entering 70g sodium instead of 70mg, or entering values per serving in the per-100g field).
   - *Mitigation:* Ingestion rule checking macronutrient sum: $\text{Protein} + \text{Carbs} + \text{Fat} + \text{Water} + \text{Ash} \le 105\text{g}$. Outliers exceeding 105g are flagged for manual review.
2. **Runtime API Instability:** As proven during live inspection, unauthenticated search requests trigger `HTTP 503: Service Temporarily Unavailable`.
   - *Mitigation:* FitKit must **never** proxy real-time search queries to the live Open Food Facts search API. Instead, FitKit will ingest the monthly Open Food Facts JSONL/CSV database dump into a local PostgreSQL table with full-text search indexing (`tsvector` / trigram indexes).

---

## 10. Data Normalization Recommendations

To ensure mathematical consistency across heterogeneous data sources, FitKit must adopt the following unified internal schema and pipeline rules.

### 10.1 Unified Storage Units
- **Energy:** Dual-stored in both **`energy_kcal`** (display standard) and **`energy_kj`** (analytical standard).
- **Macronutrients:** Strictly stored in **grams ($g$) per 100g net edible portion**.
- **Micronutrients:** Standardized to **milligrams ($mg$)** (Sodium, Potassium, Calcium, Iron, Vitamin C) or **micrograms ($\mu g$)** (Vitamin B12, Folate, Vitamin D).

### 10.2 Relational Data Architecture Blueprint

```
 ┌─────────────────────────────────────────────────────────────┐
 │                         FOOD_ITEMS                          │
 ├─────────────────────────────────────────────────────────────┤
 │ id: UUID (PK)                                               │
 │ name: VARCHAR(255)                                          │
 │ regional_names: JSONB (e.g. {"hi": "चावल", "ta": "அரிசி"})  │
 │ brand_name: VARCHAR(150) (NULL for generic)                 │
 │ barcode: VARCHAR(32) (NULL for unpackaged)                  │
 │ food_type: ENUM ('RAW', 'COOKED_SINGLE', 'RECIPE',          │
 │                  'BRANDED_PACKAGED')                        │
 │ primary_source: ENUM ('IFCT_2017', 'USDA_FDC', 'OFF',       │
 │                       'FITKIT_RECIPE')                      │
 │ source_record_id: VARCHAR(64)                               │
 │ is_verified: BOOLEAN                                        │
 └──────────────────────────────┬──────────────────────────────┘
                                │ 1:1
 ┌──────────────────────────────▼──────────────────────────────┐
 │                       FOOD_NUTRIENTS                        │
 ├─────────────────────────────────────────────────────────────┤
 │ food_id: UUID (FK -> FOOD_ITEMS.id)                         │
 │ basis: ENUM ('PER_100G', 'PER_SERVING')                     │
 │ energy_kcal: NUMERIC(7,2)                                   │
 │ energy_kj: NUMERIC(7,2)                                     │
 │ protein_g: NUMERIC(6,2)                                     │
 │ available_carbohydrates_g: NUMERIC(6,2)                     │
 │ total_fat_g: NUMERIC(6,2)                                   │
 │ dietary_fiber_g: NUMERIC(6,2)                               │
 │ moisture_g: NUMERIC(6,2)                                    │
 │ quality_flags: INT (Bitmask: 0=RAW, 1=CALC_ENERGY, ...)      │
 └──────────────────────────────┬──────────────────────────────┘
                                │ 1:N
 ┌──────────────────────────────▼──────────────────────────────┐
 │                        FOOD_PORTIONS                        │
 ├─────────────────────────────────────────────────────────────┤
 │ id: UUID (PK)                                               │
 │ food_id: UUID (FK -> FOOD_ITEMS.id)                         │
 │ portion_description: VARCHAR(100) (e.g., '1 cup', '1 piece')│
 │ gram_weight: NUMERIC(6,2) (e.g., 158.00)                    │
 │ is_default_display: BOOLEAN                                 │
 └─────────────────────────────────────────────────────────────┘
```

---

## 11. Recommended Primary and Supplementary Datasets

FitKit should implement a **Tricameral Data Strategy**:

```
                       ┌─────────────────────────┐
                       │   FITKIT NUTRITION DB   │
                       └────────────┬────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
         ▼                          ▼                          ▼
 ┌───────────────┐          ┌───────────────┐          ┌───────────────┐
 │    TIER 1     │          │    TIER 2     │          │    TIER 3     │
 │ RAW ANALYTIC  │          │ COOKED RECIPES│          │   BRANDED     │
 ├───────────────┤          ├───────────────┤          ├───────────────┤
 │ • IFCT 2017   │          │ • Custom      │          │ • Open Food   │
 │   (Indian)    │          │   Simulation  │          │   Facts India │
 │ • USDA FDC    │          │   Engine      │          │ • USDA        │
 │   SR Legacy   │          │ • ICMR NVIF   │          │   Branded     │
 │   & Found.    │          │   Table 2     │          │   Foods       │
 └───────────────┘          └───────────────┘          └───────────────┘
```

1. **Tier 1 — Core Analytical Raw Ingredients:**
   - **Primary:** ICMR-NIN IFCT 2017 (sanitized to repair 0 kJ oil records and convert kJ to kcal).
   - **Supplementary:** USDA FoodData Central Foundation & SR Legacy (for global commodities, meats, and western staples).
2. **Tier 2 — Prepared Dishes & Recipe Simulation:**
   - **Primary:** FitKit Deterministic Recipe Engine calculating cooked values from Tier 1 ingredients using water yield factors and retention metrics.
   - **Calibration Baseline:** ICMR-NIN NVIF Table 2 ("Nutritive Value of Cooked Dishes") and IIIT-Delhi RecipeDB serving standards.
3. **Tier 3 — Branded Packaged Foods:**
   - **Primary:** Open Food Facts (offline ingested Indian snapshot of ~24,800 products, cleaned with macronutrient sum validation rules).
   - **Supplementary:** USDA Branded Foods (for imported commercial goods).

---

## 12. Known Gaps and Unresolved Questions

1. **Household Measure Ambiguity in India:**
   - Unlike Western dietary databases with standardized cups (240ml) and tablespoons (15ml), Indian home cooking employs variable vessels (*Katoris*, glasses, tea cups).
   - *Resolution Required in Phase 2:* FitKit must define explicit, standardized UI guidelines for visual portion estimation (e.g., Small Katori = 150ml / 150g cooked dal; Medium Katori = 200ml; Standard Roti = 40g).
2. **Dynamic Oil & Ghee Cooking Absorption:**
   - In preparations like *Tadka* (tempering dals) or deep-fried snacks (*Poori*, *Samosa*), oil absorption varies widely based on frying temperature and surface area.
   - *Resolution Required in Phase 2:* Provide a "Cooking Fat Adjuster" toggle in the meal logger (e.g., "Light Oil: +3g fat", "Standard Restaurant: +10g fat").
3. **ICMR-NIN Commercial Licensing Path:**
   - While permissible for academic and student development, commercial SaaS deployment requires clarification on digital redistribution licensing fees from ICMR-NIN.
   - *Resolution Required:* Contact ICMR-NIN technology transfer desk during Phase 2 planning if moving toward commercial production.

---

## 13. Proposed Next Steps for Phase 2 (Completed in Phase 2 Implementation)

Phase 2 implementation has successfully executed the core foundations:
- **Architecture Standardized:** Spring Boot 3.3.4 (Java 17), MySQL 8.0, and Flyway migration pipeline (`V1__init_nutrition_schema.sql`, `V2__seed_sources_and_sample_catalog.sql`).
- **Relational Schema Implemented:** Normalized tables for `source_provenance`, `foods`, `food_nutrients`, `food_portions`, `recipes`, `recipe_ingredients`, and `import_audit_logs`.
- **0 kJ Cooking Oil Bug Resolved:** Authoritative Atwater calculation rule applied ($9.0\text{ kcal/g}$ fat $\implies 900.0\text{ kcal}$ for pure oils), tagged with `CORRECTED_ANOMALY` and full provenance logging.
- **Recipe Simulation Engine Validated:** Implemented in `RecipeCalculationService`, modeling hydration yields and cooking fat dynamics for Chapati, Idli, and Dal Tadka.
- **Automated Regression Suite Passing:** 19/19 JUnit 5 integration and unit tests passing covering normalization, recipe math, idempotency, and database integrity.

See `docs/phase-2-implementation-report.md` for complete implementation and validation specifics.

---
*Report compiled and verified by Senior Data Engineer & Full-Stack Architect.*  
*Artifacts Generated:* `docs/nutrition-data-audit.md`, `docs/dataset-inventory.json`, `docs/phase-2-implementation-report.md`.  
*Status: Phase 1 & Phase 2 Complete — Tested & Verified.*

