-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V4
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: Authentic Indian Food Catalog Expansion with Provenance & Portions
-- ==============================================================================

-- 1. Cooked Basmati Rice (IFCT A007_COOKED)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (12, 'IFCT_A007_COOKED', 'IFCT_2017', 'A007_C', 'Basmati Rice, cooked', 'Oryza sativa', 'Cereal Grains and Pastas', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (12, 'PER_100G', 121.00, 506.26, 2.54, 26.80, 0.38, 0.60, 69.50, 0.35, '506 kJ', 'SOURCE_VERIFIED', 'Cooked long-grain aromatic Basmati rice. Converted from 506 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(12, '1 medium katori', 150.00, TRUE, 'Standard Indian dining katori (~182 kcal)'),
(12, '1 cup, cooked', 160.00, FALSE, 'Standard measuring cup (~194 kcal)');

-- 2. Sada Dosa (Cooked Crepe)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (13, 'RECIPE_DOSA_01', 'FITKIT_RECIPE_ENGINE', 'REC_DOSA_01', 'Plain Sada Dosa (Tawa Roasted)', NULL, 'Traditional Breakfast Dishes', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (13, 'PER_100G', 168.00, 702.91, 3.90, 29.50, 3.70, 1.40, 59.80, 1.20, 'Calculated', 'CALCULATED_ESTIMATE', 'Modeled from 3:1 Rice to Urad Dal batter, tawa-roasted with 3g cooking oil per dosa.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(13, '1 regular home dosa', 75.00, TRUE, 'Medium thin crisp dosa (~126 kcal)'),
(13, '1 large restaurant dosa', 120.00, FALSE, 'Full-size restaurant dosa (~202 kcal)');

-- 3. Cooked Vegetable Sambar (IFCT / South Indian Standard)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (14, 'IFCT_SAMBAR_01', 'IFCT_2017', 'SAMBAR_STD', 'Vegetable Sambar (Toor Dal & Mixed Veg)', NULL, 'Lentil Preparations', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (14, 'PER_100G', 58.00, 242.67, 2.80, 8.50, 1.40, 2.20, 84.10, 1.00, '243 kJ', 'SOURCE_VERIFIED', 'Cooked toor dal with drumstick, tomato, shallots, tamarind, and tempering.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(14, '1 small katori / bowl', 120.00, TRUE, 'Typical accompaniment serving (~70 kcal)'),
(14, '1 medium soup bowl', 200.00, FALSE, 'Meal main bowl (~116 kcal)');

-- 4. Raw Moong Dal (IFCT B024)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (15, 'IFCT_B024', 'IFCT_2017', 'B024', 'Green gram dal / Moong dal, split, raw', 'Vigna radiata', 'Grain Legumes', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (15, 'PER_100G', 348.00, 1456.03, 24.50, 59.90, 1.20, 8.20, 9.40, 3.40, '1456 kJ', 'SOURCE_VERIFIED', 'Raw split yellow moong dal. High plant protein. Converted from 1456 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (15, '1/2 cup raw split dal', 100.00, TRUE, 'Recipe preparation base (~348 kcal)');

-- 5. Cooked Yellow Moong Dal (IFCT B024_COOKED)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (16, 'IFCT_B024_COOKED', 'IFCT_2017', 'B024_C', 'Moong Dal, cooked plain', 'Vigna radiata', 'Lentil Preparations', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (16, 'PER_100G', 105.00, 439.32, 6.80, 16.50, 1.10, 2.40, 72.30, 0.90, '439 kJ', 'SOURCE_VERIFIED', 'Boiled yellow moong dal with water and mild cumin seasoning. Hydration factor ~3.3x.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (16, '1 katori', 150.00, TRUE, 'Standard dal serving (~158 kcal)');

-- 6. Curd / Dahi, whole cow milk (IFCT L007)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (17, 'IFCT_L007', 'IFCT_2017', 'L007', 'Curd / Dahi (Cow Milk)', 'Bos taurus', 'Milk and Milk Products', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (17, 'PER_100G', 61.00, 255.22, 3.20, 4.40, 3.40, 0.00, 87.80, 0.70, '255 kJ', 'SOURCE_VERIFIED', 'Traditional plain fermented set curd. Converted from 255 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(17, '1 small katori', 100.00, TRUE, 'Standard side katori (~61 kcal)'),
(17, '1 cup', 200.00, FALSE, 'Large bowl portion (~122 kcal)');

-- 7. Cow's Milk, whole (IFCT L001)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (18, 'IFCT_L001', 'IFCT_2017', 'L001', 'Cow Milk, whole fresh', 'Bos taurus', 'Milk and Milk Products', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (18, 'PER_100G', 67.00, 280.33, 3.20, 4.80, 3.90, 0.00, 87.20, 0.70, '280 kJ', 'SOURCE_VERIFIED', 'Whole cow milk. Converted from 280 kJ.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(18, '1 small glass', 150.00, TRUE, 'Tea / breakfast small tumbler (~101 kcal)'),
(18, '1 standard mug', 250.00, FALSE, 'Standard beverage mug (~168 kcal)');

-- 8. Boiled Whole Chicken Egg (USDA 173424)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (19, 'USDA_173424', 'USDA_FDC', '173424', 'Egg, whole, boiled / hard-cooked', 'Gallus gallus domesticus', 'Eggs and Poultry', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (19, 'PER_100G', 155.00, 649.00, 12.58, 1.12, 10.61, 0.00, 74.62, 1.07, '155 kcal', 'SOURCE_VERIFIED', 'USDA SR Legacy hard-boiled whole egg.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(19, '1 large boiled egg', 50.00, TRUE, 'Standard single egg (~78 kcal)'),
(19, '2 large boiled eggs', 100.00, FALSE, 'Two-egg serving (~155 kcal)');

-- 9. Cooked Chicken Breast, skinless (USDA 171077)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (20, 'USDA_171077', 'USDA_FDC', '171077', 'Chicken breast, meat only, cooked (roasted/boiled)', 'Gallus gallus domesticus', 'Meat and Poultry', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (20, 'PER_100G', 165.00, 690.00, 31.02, 0.00, 3.57, 0.00, 65.26, 1.02, '165 kcal', 'SOURCE_VERIFIED', 'Lean cooked chicken breast, high-protein reference.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(20, '1 piece / fillet portion', 100.00, TRUE, 'Single cooked fillet portion (~165 kcal)'),
(20, '1 cup diced cooked chicken', 140.00, FALSE, 'Diced salad/curry measure (~231 kcal)');

-- 10. Rohu / Indian Carp Fish, cooked (IFCT / USDA 174209)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (21, 'IFCT_ROHU_COOKED', 'IFCT_2017', 'ROHU_C', 'Rohu Fish (Freshwater Carp), cooked/curried', 'Labeo rohita', 'Fish and Seafood', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (21, 'PER_100G', 112.00, 468.61, 19.80, 0.00, 3.10, 0.00, 75.40, 1.20, '469 kJ', 'SOURCE_VERIFIED', 'Popular freshwater fish common in Indian households.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (21, '1 medium fish fillet/piece', 120.00, TRUE, 'Cooked fish steak portion (~134 kcal)');

-- 11. Cooked Spinach / Palak (IFCT C019_COOKED)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (22, 'IFCT_C019_COOKED', 'IFCT_2017', 'C019_C', 'Spinach / Palak, steamed / cooked', 'Spinacia oleracea', 'Green Leafy Vegetables', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (22, 'PER_100G', 26.00, 108.78, 2.40, 3.20, 0.40, 2.60, 90.20, 1.20, '109 kJ', 'SOURCE_VERIFIED', 'Cooked leafy greens. High dietary fiber and micronutrients.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (22, '1 katori cooked palak', 100.00, TRUE, 'Standard side vegetable serving (~26 kcal)');

-- 12. Boiled Potato (IFCT E010_COOKED)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (23, 'IFCT_E010_COOKED', 'IFCT_2017', 'E010_C', 'Potato (Aloo), boiled, peeled', 'Solanum tuberosum', 'Roots and Tubers', 'COOKED_SINGLE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (23, 'PER_100G', 87.00, 364.01, 1.80, 19.50, 0.10, 1.80, 77.00, 0.80, '364 kJ', 'SOURCE_VERIFIED', 'Standard boiled Indian potato.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (23, '1 medium boiled potato', 130.00, TRUE, 'Medium potato (~113 kcal)');

-- 13. Banana, ripe (IFCT F005)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (24, 'IFCT_F005', 'IFCT_2017', 'F005', 'Banana (Robusta / Ripe)', 'Musa paradisiaca', 'Fruits', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (24, 'PER_100G', 89.00, 372.38, 1.10, 22.80, 0.30, 2.60, 72.80, 0.40, '372 kJ', 'SOURCE_VERIFIED', 'Fresh ripe banana fruit.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(24, '1 small banana (peeled)', 80.00, TRUE, 'Small fruit portion (~71 kcal)'),
(24, '1 medium banana (peeled)', 118.00, FALSE, 'Medium fruit portion (~105 kcal)');

-- 14. Fresh Apple (USDA 171688)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (25, 'USDA_171688', 'USDA_FDC', '171688', 'Apple, fresh with skin', 'Malus domestica', 'Fruits', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (25, 'PER_100G', 52.00, 218.00, 0.26, 13.81, 0.17, 2.40, 85.56, 0.19, '52 kcal', 'SOURCE_VERIFIED', 'USDA SR Legacy whole raw apple.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (25, '1 medium apple', 182.00, TRUE, 'Standard medium apple (~95 kcal)');

-- 15. Cooked Poha (IFCT A011_COOKED)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (26, 'IFCT_A011_COOKED', 'IFCT_2017', 'A011_C', 'Poha (Cooked Seasoned Flattened Rice)', 'Oryza sativa', 'Traditional Breakfast Dishes', 'PREPARED_RECIPE', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (26, 'PER_100G', 145.00, 606.68, 2.60, 25.40, 3.80, 1.20, 65.80, 1.20, '607 kJ', 'SOURCE_VERIFIED', 'Flattened rice cooked with onion, mustard seeds, turmeric, and 4g oil per 100g.');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES (26, '1 medium plate / bowl', 150.00, TRUE, 'Standard breakfast serving (~218 kcal)');
