-- ==============================================================================
-- FitKit Nutrition Data Seed Migration: V2
-- Target Database: MySQL 8.0 (ANSI SQL / H2 MySQL Mode compatible)
-- Purpose: Initial verified seed provenance, canonical ingredients, and recipes
-- ==============================================================================

-- 1. Insert Source Provenance
INSERT INTO source_provenance (id, dataset_name, publisher, version_or_date, license_type, official_url, is_commercial_allowed, citation_text)
VALUES
('IFCT_2017', 'Indian Food Composition Tables 2017', 'National Institute of Nutrition (NIN), ICMR, India', '2017 (Rev 2018)', 'ICMR-NIN Copyright (Academic / Fair Use)', 'https://www.nin.res.in/ebooks/IFCT2017.pdf', FALSE, 'Longvah T, Ananthan R, Bhaskarachary K, Venkaiah K. Indian Food Composition Tables. National Institute of Nutrition, ICMR, Hyderabad, 2017.'),
('USDA_FDC', 'USDA FoodData Central (SR Legacy & FNDDS)', 'Agricultural Research Service, USDA', 'October 2024', 'Public Domain (CC0 1.0)', 'https://fdc.nal.usda.gov/', TRUE, 'U.S. Department of Agriculture, Agricultural Research Service. FoodData Central. fdc.nal.usda.gov.'),
('OPEN_FOOD_FACTS', 'Open Food Facts (India Packaged Foods)', 'Open Food Facts Association', '2026 Snapshot', 'Open Database License (ODbL 1.0)', 'https://in.openfoodfacts.org/', TRUE, 'Open Food Facts contributors. Open Food Facts database. openfoodfacts.org.'),
('FITKIT_RECIPE_ENGINE', 'FitKit Indian Recipe Simulation Engine', 'FitKit Core Computational Nutrition Team', '1.0.0', 'Proprietary / Algorithmic Model', 'https://fitkit.local/recipes', TRUE, 'FitKit deterministic recipe simulation modeling raw ingredients, cooking yield factors, and nutrient retention factors.');

-- 2. Insert Core Seed Foods (Raw, Cooked, Oil, Dairy, Branded)
-- Food 1: Raw White Rice (SR Legacy 168878)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (1, 'USDA_168878', 'USDA_FDC', '168878', 'Rice, white, long-grain, regular, raw, unenriched', 'Oryza sativa', 'Cereal Grains and Pastas', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (1, 'PER_100G', 365.00, 1530.00, 6.67, 79.95, 0.66, 1.30, 11.62, 1.10, '365 kcal', 'SOURCE_VERIFIED', 'USDA SR Legacy laboratory analytical profile.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (1, '1 cup, dry uncooked', 185.00, TRUE, 'Standard USDA measure for raw grains');

-- Food 2: Cooked White Rice (Survey FNDDS 2345388) - Demonstrating Water Hydration Expansion (Yield 2.8x)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (2, 'USDA_2345388', 'USDA_FDC', '2345388', 'Rice, white, cooked, regular', 'Oryza sativa', 'Rice and pasta dishes', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (2, 'PER_100G', 130.00, 544.00, 2.69, 28.17, 0.28, 0.40, 68.44, 0.42, '130 kcal', 'SOURCE_VERIFIED', 'Hydration expansion yield ≈ 2.8x. 68.44% moisture content.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(2, '1 cup, cooked', 158.00, TRUE, 'Standard cooked rice cup'),
(2, '1 small katori (heaped)', 120.00, FALSE, 'Indian household katori serving');

-- Food 3: Raw Red Gram Dal / Toor Dal (IFCT B021)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (3, 'IFCT_B021', 'IFCT_2017', 'B021', 'Red gram, dal (Toor dal / Arhar dal)', 'Cajanus cajan', 'Grain Legumes', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (3, 'PER_100G', 330.80, 1384.00, 21.70, 55.23, 1.56, 9.06, 9.20, 3.25, '1384 kJ', 'SOURCE_VERIFIED', 'Converted from 1384 kJ (1384 / 4.184 = 330.8 kcal). Raw dry pulse.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (3, '1/2 cup raw split dal', 100.00, TRUE, 'Typical dry dal recipe base');

-- Food 4: Ghee / Clarified Butter (IFCT T013) - Demonstrating 0 kJ Anomaly Resolution
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (4, 'IFCT_T013', 'IFCT_2017', 'T013', 'Ghee (Clarified Butter)', 'Bubalus bubalis / Bos taurus', 'Edible Oils and Fats', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (4, 'PER_100G', 900.00, 3765.60, 0.00, 0.00, 100.00, 0.00, 0.00, 0.00, '0 kJ', 'CORRECTED_ANOMALY', 'CORRECTION AUDIT: Source digitized record reported 0 kJ despite 100g lipid. Corrected via 9.0 kcal/g Atwater standard (100g * 9.0 = 900.0 kcal / 3765.6 kJ).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(4, '1 teaspoon (tsp)', 5.00, TRUE, 'Standard Indian home cooking spoon (~45 kcal)'),
(4, '1 tablespoon (tbsp)', 14.00, FALSE, 'Cooking fat measure (~126 kcal)');

-- Food 5: Fresh Paneer (IFCT L003) - Dairy Protein
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (5, 'IFCT_L003', 'IFCT_2017', 'L003', 'Paneer (Fresh Cottage Cheese)', 'Bubalus bubalis milk', 'Milk and Milk Products', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (5, 'PER_100G', 257.90, 1079.00, 18.86, 12.41, 14.78, 0.00, 51.96, 2.00, '1079 kJ', 'SOURCE_VERIFIED', 'Converted from 1079 kJ (1079 / 4.184 = 257.9 kcal). Artisanal fresh paneer.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(5, '100g block', 100.00, TRUE, 'Standard retail portion'),
(5, '1 cup paneer cubes', 150.00, FALSE, 'Cubed paneer for curries');

-- Food 6: Amul Pasteurized Butter (Open Food Facts Packaged Retail)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (6, 'OFF_8901262010014', 'OPEN_FOOD_FACTS', '8901262010014', 'Amul Pasteurized Butter', NULL, 'Dairy / Spreads', 'PACKAGED_COMMERCIAL', 'Amul', '8901262010014', TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (6, 'PER_100G', 714.30, 2988.60, 0.00, 0.00, 78.57, 0.00, 16.00, 2.50, '714.3 kcal', 'SOURCE_VERIFIED', 'Authentic Indian commercial packaging declaration. Fat 78.57%, salt, moisture.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (6, '1 single-serve pat', 10.00, TRUE, 'Packaged single-serve pat (~71 kcal)');

-- Food 7: Whole Wheat Flour / Atta (IFCT A019 Raw Ingredient for Roti Recipe)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (7, 'IFCT_A019', 'IFCT_2017', 'A019', 'Wheat flour, whole (Atta)', 'Triticum aestivum', 'Cereals and Millets', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (7, 'PER_100G', 320.30, 1340.00, 10.57, 61.20, 1.72, 11.23, 13.80, 1.48, '1340 kJ', 'SOURCE_VERIFIED', 'Whole wheat flour rich in dietary fiber (11.23g). Converted from 1340 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (7, '1 cup atta flour', 120.00, TRUE, 'Volume measure for dry dough preparation');

-- Food 8: Black Gram Dal / Urad Dal (IFCT B003 Raw Ingredient for Idli Recipe)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (8, 'IFCT_B003', 'IFCT_2017', 'B003', 'Black gram, dal (Urad dal)', 'Vigna mungo', 'Grain Legumes', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (8, 'PER_100G', 324.10, 1356.00, 23.06, 51.00, 1.69, 11.93, 9.16, 3.16, '1356 kJ', 'SOURCE_VERIFIED', 'Raw split urad dal. Converted from 1356 kJ.');

-- 3. Insert Synthesized Cooked Recipe Foods & Recipe Definitions
-- Recipe 1: Traditional Whole Wheat Chapati / Roti
-- 100g dry Atta (320.3 kcal) + 65g water -> 165g dough. Baked on tawa -> 132g cooked (20% moisture loss). Yield = 1.32.
-- Makes 4 chapatis (33g - 40g each). Per 100g cooked: ~242 kcal, 8.0g protein, 46.4g carbs, 1.3g fat, 8.5g fiber.
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (9, 'RECIPE_CHAPATI_01', 'FITKIT_RECIPE_ENGINE', 'REC_CHAP_01', 'Chapati / Phulka (Whole Wheat Flatbread)', NULL, 'Traditional Breads', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (9, 'PER_100G', 242.65, 1015.25, 8.01, 46.36, 1.30, 8.51, 34.70, 1.12, 'Calculated', 'CALCULATED_ESTIMATE', 'Modeled from 100g whole wheat atta with 1.32x tawa baking yield factor (20% moisture loss from dough). Without added ghee.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(9, '1 medium chapati (no oil)', 35.00, TRUE, 'Standard domestic chapati (approx 85 kcal)'),
(9, '1 large chapati', 50.00, FALSE, 'Restaurant / large home chapati (approx 121 kcal)');

INSERT INTO recipes (id, recipe_code, food_id, title, cuisine_region, total_raw_weight_g, cooking_yield_factor, cooked_weight_g, servings_yield, serving_weight_g, cooking_method, preparation_notes, uncertainty_rating)
VALUES (1, 'REC_CHAP_01', 9, 'Traditional Whole Wheat Chapati', 'North Indian / National', 100.00, 1.320, 132.00, 4.00, 33.00, 'ROASTING_TAWA', 'Kneaded with water, rolled thin, dry roasted on hot tawa until puffed. No added oil.', 'LOW_STANDARDIZED');

INSERT INTO recipe_ingredients (recipe_id, food_id, raw_quantity_g, unit_measure, retention_factor_override, notes)
VALUES (1, 7, 100.00, 'g', 1.000, 'Whole wheat atta flour base');

-- Recipe 2: Steamed Idli
-- 150g raw rice (547.5 kcal) + 50g urad dal (162 kcal) = 200g raw ingredients. Fermented with water.
-- Steaming yield = ~2.40x. Cooked weight = 480g (10 idlis of 48g each).
-- Per 100g cooked idli: ~147.8 kcal, 4.45g protein, 31.4g carbs, 0.46g fat, 1.7g fiber.
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (10, 'RECIPE_IDLI_01', 'FITKIT_RECIPE_ENGINE', 'REC_IDLI_01', 'Steamed Idli (Rice and Urad Dal Cake)', NULL, 'Traditional Breakfast Dishes', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (10, 'PER_100G', 147.81, 618.45, 4.45, 31.38, 0.46, 1.72, 61.20, 0.79, 'Calculated', 'CALCULATED_ESTIMATE', 'Modeled from 3:1 Rice to Urad Dal batter, fermented and steamed with 2.4x hydration yield factor.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(10, '1 standard idli', 50.00, TRUE, 'Standard South Indian restaurant/home idli (~74 kcal)'),
(10, '2 idlis (serving)', 100.00, FALSE, 'Standard breakfast serving (~148 kcal)');

INSERT INTO recipes (id, recipe_code, food_id, title, cuisine_region, total_raw_weight_g, cooking_yield_factor, cooked_weight_g, servings_yield, serving_weight_g, cooking_method, preparation_notes, uncertainty_rating)
VALUES (2, 'REC_IDLI_01', 10, 'Steamed Idli', 'South Indian', 200.00, 2.400, 480.00, 10.00, 48.00, 'STEAMING', 'Soaked rice and dal ground to smooth batter, naturally fermented for 12 hours, poured into greased moulds and steamed for 10 minutes.', 'LOW_STANDARDIZED');

INSERT INTO recipe_ingredients (recipe_id, food_id, raw_quantity_g, unit_measure, retention_factor_override, notes)
VALUES 
(2, 1, 150.00, 'g', 1.000, 'Raw rice (parboiled / idli rice)'),
(2, 8, 50.00, 'g', 1.000, 'Raw whole/split black gram (urad dal)');

-- Recipe 3: Homestyle Dal Tadka
-- 100g raw toor dal (330.8 kcal) + 5g ghee (45 kcal) = 375.8 kcal. Cooked with water.
-- Boiled dal yield = 3.20x. Cooked weight = 320g.
-- Per 100g cooked dal: ~117.4 kcal, 6.78g protein, 17.26g carbs, 2.05g fat, 2.83g fiber.
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (11, 'RECIPE_DAL_TADKA_01', 'FITKIT_RECIPE_ENGINE', 'REC_DAL_01', 'Dal Tadka (Cooked Toor Dal with Ghee Tempering)', NULL, 'Lentil Preparations', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (11, 'PER_100G', 117.44, 491.37, 6.78, 17.26, 2.05, 2.83, 70.10, 0.98, 'Calculated', 'CALCULATED_ESTIMATE', 'Modeled from 100g raw toor dal pressure cooked with water (yield 3.2x) and tempered with 5g ghee.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(11, '1 medium katori (serving)', 150.00, TRUE, 'Standard Indian dining katori (~176 kcal)'),
(11, '1 cup cooked dal', 240.00, FALSE, 'Large bowl / measuring cup (~282 kcal)');

INSERT INTO recipes (id, recipe_code, food_id, title, cuisine_region, total_raw_weight_g, cooking_yield_factor, cooked_weight_g, servings_yield, serving_weight_g, cooking_method, preparation_notes, uncertainty_rating)
VALUES (3, 'REC_DAL_01', 11, 'Homestyle Dal Tadka', 'Pan-Indian', 105.00, 3.048, 320.00, 2.13, 150.00, 'PRESSURE_COOKING', 'Toor dal pressure cooked with turmeric, salt and water. Tempered with 1 tsp ghee, cumin, mustard seeds and chili.', 'MODERATE_HOME_VARIANCE');

INSERT INTO recipe_ingredients (recipe_id, food_id, raw_quantity_g, unit_measure, retention_factor_override, notes)
VALUES 
(3, 3, 100.00, 'g', 1.000, 'Raw red gram (toor dal)'),
(3, 4, 5.00, 'g', 1.000, 'Ghee for tempering (tadka)');
