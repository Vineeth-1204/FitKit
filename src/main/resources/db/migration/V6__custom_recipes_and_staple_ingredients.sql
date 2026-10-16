-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V6
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: Custom Recipes Support (User Ownership, Soft Delete) & Staple Recipe Ingredients
-- ==============================================================================

-- 1. Extend Recipes Table with Ownership and Status Columns
ALTER TABLE recipes ADD COLUMN user_id VARCHAR(191) NULL;
ALTER TABLE recipes ADD COLUMN is_custom BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE recipes ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE recipes ADD COLUMN description VARCHAR(500) NULL;

CREATE INDEX idx_recipes_user_id ON recipes(user_id);
CREATE INDEX idx_recipes_is_custom ON recipes(is_custom);

-- 2. Insert Core Raw Recipe Ingredients (IFCT 2017)
-- Food 27: Semolina / Suji / Rava (IFCT A018)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (27, 'IFCT_A018', 'IFCT_2017', 'A018', 'Semolina / Suji / Rava', 'Triticum durum', 'Cereal Grains and Pastas', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (27, 'PER_100G', 340.00, 1422.56, 10.40, 72.80, 0.80, 3.40, 11.80, 0.80, '1423 kJ', 'SOURCE_VERIFIED', 'Dry semolina/suji flour baseline for upma, halwa and porridge. Converted from 1423 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(27, '1/2 cup (dry measure)', 90.00, TRUE, 'Typical dry upma recipe base (~306 kcal)'),
(27, '1 cup (dry measure)', 180.00, FALSE, 'Large recipe measure (~612 kcal)');

-- Food 28: Fresh Onion, raw (IFCT C012)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (28, 'IFCT_C012', 'IFCT_2017', 'C012', 'Onion, fresh raw', 'Allium cepa', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (28, 'PER_100G', 50.00, 209.20, 1.20, 11.10, 0.10, 2.50, 84.30, 0.80, '209 kJ', 'SOURCE_VERIFIED', 'Raw red onion. Base for tadka and curries. Converted from 209 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(28, '1 medium onion', 70.00, TRUE, 'Standard domestic cooking onion (~35 kcal)'),
(28, '1 small onion', 45.00, FALSE, 'Small garnish onion (~23 kcal)');

-- Food 29: Fresh Tomato, ripe raw (IFCT C035)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (29, 'IFCT_C035', 'IFCT_2017', 'C035', 'Tomato, ripe raw', 'Solanum lycopersicum', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (29, 'PER_100G', 20.00, 83.68, 0.90, 3.60, 0.20, 1.70, 93.10, 0.50, '84 kJ', 'SOURCE_VERIFIED', 'Fresh ripe red tomatoes. High water content. Converted from 84 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(29, '1 medium tomato', 90.00, TRUE, 'Standard domestic cooking tomato (~18 kcal)'),
(29, '1 small tomato', 60.00, FALSE, 'Small salad tomato (~12 kcal)');

-- Food 30: Green Chili, fresh raw (IFCT D005)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (30, 'IFCT_D005', 'IFCT_2017', 'D005', 'Green Chili, fresh raw', 'Capsicum annuum', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (30, 'PER_100G', 45.00, 188.28, 2.60, 7.80, 0.60, 6.80, 81.30, 0.90, '188 kJ', 'SOURCE_VERIFIED', 'Fresh pungent green chili pepper. Converted from 188 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(30, '1 small green chili', 5.00, TRUE, 'Single chili seasoning (~2 kcal)'),
(30, '2 green chilies', 10.00, FALSE, 'Double chili portion (~5 kcal)');

-- Food 31: Cumin Seeds / Jeera, whole dry (IFCT D004)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (31, 'IFCT_D004', 'IFCT_2017', 'D004', 'Cumin Seeds (Jeera)', 'Cuminum cyminum', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (31, 'PER_100G', 375.00, 1569.00, 17.80, 44.20, 22.30, 10.50, 8.10, 7.70, '1569 kJ', 'SOURCE_VERIFIED', 'Aromatic whole cumin seeds. Converted from 1569 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(31, '1 teaspoon (tsp)', 5.00, TRUE, 'Standard tempering spoon (~19 kcal)'),
(31, '1 tablespoon (tbsp)', 12.00, FALSE, 'Large recipe measure (~45 kcal)');

-- Food 32: Turmeric Powder / Haldi (IFCT D028)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (32, 'IFCT_D028', 'IFCT_2017', 'D028', 'Turmeric Powder (Haldi)', 'Curcuma longa', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (32, 'PER_100G', 349.00, 1460.22, 6.30, 69.40, 5.10, 21.10, 11.40, 6.80, '1460 kJ', 'SOURCE_VERIFIED', 'Ground dry turmeric rhizome. Converted from 1460 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(32, '1/2 teaspoon (tsp)', 2.00, TRUE, 'Typical dish pinch (~7 kcal)'),
(32, '1 teaspoon (tsp)', 4.00, FALSE, 'Full teaspoon measure (~14 kcal)');

-- Food 33: Mustard Oil, cold-pressed raw (IFCT T017)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (33, 'IFCT_T017', 'IFCT_2017', 'T017', 'Mustard Oil, cold-pressed', 'Brassica juncea', 'Edible Oils and Fats', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (33, 'PER_100G', 900.00, 3765.60, 0.00, 0.00, 100.00, 0.00, 0.00, 0.00, '0 kJ', 'CORRECTED_ANOMALY', 'CORRECTION AUDIT: Source recorded 0 kJ despite 100g lipids. Corrected to 900.0 kcal (3765.6 kJ) per 9.0 kcal/g standard.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(33, '1 teaspoon (tsp)', 5.00, TRUE, 'Cooking measure (~45 kcal)'),
(33, '1 tablespoon (tbsp)', 14.00, FALSE, 'Cooking oil measure (~126 kcal)');
