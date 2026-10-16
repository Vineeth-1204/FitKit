-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V8
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: Phase 7 — Expand Indian Food Catalog & Add Food Search Aliases Table
-- ==============================================================================

-- 1. Create Food Aliases Table
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


-- 2. Expand Food Catalog with 18 Essential Indian Staples (Foods 34 to 51)
-- Source: Indian Food Composition Tables (IFCT 2017), ICMR-NIN

-- Food 34: Brown Rice, raw (IFCT A008)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (34, 'IFCT_A008', 'IFCT_2017', 'A008', 'Brown Rice, raw grain', 'Oryza sativa', 'Cereal Grains and Pastas', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (34, 'PER_100G', 353.20, 1478.00, 7.94, 70.82, 2.75, 4.80, 12.18, 1.51, '1478 kJ', 'SOURCE_VERIFIED', 'Whole grain brown rice raw kernel baseline. Source energy 1478 kJ (353.2 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(34, '1/2 cup raw (dry)', 90.00, TRUE, 'Standard dry grain recipe portion (~318 kcal)'),
(34, '1 cup raw (dry)', 185.00, FALSE, 'Large recipe measure (~653 kcal)');


-- Food 35: Rolled Oats / Oatmeal, raw (IFCT A015)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (35, 'IFCT_A015', 'IFCT_2017', 'A015', 'Rolled Oats / Oatmeal, raw flakes', 'Avena sativa', 'Cereal Grains and Pastas', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (35, 'PER_100G', 374.30, 1566.00, 13.15, 63.30, 6.90, 10.40, 9.80, 1.90, '1566 kJ', 'SOURCE_VERIFIED', 'Whole oat grain flakes. Source energy 1566 kJ (374.3 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(35, '1/2 cup rolled oats (dry)', 40.00, TRUE, 'Standard breakfast porridge measure (~150 kcal)'),
(35, '1 cup rolled oats (dry)', 80.00, FALSE, 'Full cup dry oatmeal (~299 kcal)');


-- Food 36: Besan / Bengal Gram Flour (IFCT B006)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (36, 'IFCT_B006', 'IFCT_2017', 'B006', 'Besan / Bengal Gram Flour', 'Cicer arietinum', 'Grain Legumes and Pulses', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (36, 'PER_100G', 365.70, 1530.00, 21.80, 56.60, 5.30, 12.80, 10.40, 2.70, '1530 kJ', 'SOURCE_VERIFIED', 'Finely ground Bengal gram flour for chilla, pakora and curries. Source energy 1530 kJ (365.7 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(36, '1/4 cup (dry measure)', 30.00, TRUE, 'Typical chilla / cheela pancake measure (~110 kcal)'),
(36, '1 tablespoon (tbsp)', 10.00, FALSE, 'Curry thickening measure (~37 kcal)'),
(36, '1 cup (dry measure)', 120.00, FALSE, 'Large recipe batter measure (~439 kcal)');


-- Food 37: Raw Poha / Rice Flakes / Flattened Rice (IFCT A011)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (37, 'IFCT_A011', 'IFCT_2017', 'A011', 'Raw Poha / Flattened Rice Flakes', 'Oryza sativa', 'Cereal Grains and Pastas', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (37, 'PER_100G', 354.00, 1481.00, 6.70, 77.20, 1.20, 2.80, 10.80, 1.30, '1481 kJ', 'SOURCE_VERIFIED', 'Uncooked dry flattened rice flakes (avalakki). Source energy 1481 kJ (354 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(37, '1/2 cup raw flakes', 40.00, TRUE, 'Single serving dry poha measure (~142 kcal)'),
(37, '1 cup raw flakes', 80.00, FALSE, 'Double serving dry poha measure (~283 kcal)');


-- Food 38: Chana Dal / Split Bengal Gram, raw (IFCT B005)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (38, 'IFCT_B005', 'IFCT_2017', 'B005', 'Chana Dal / Split Bengal Gram, raw', 'Cicer arietinum', 'Grain Legumes and Pulses', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (38, 'PER_100G', 370.00, 1548.00, 21.55, 57.82, 5.30, 15.20, 9.80, 2.60, '1548 kJ', 'SOURCE_VERIFIED', 'Dry split deskinned Bengal gram legume. Source energy 1548 kJ (370 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(38, '1/4 cup raw dal', 50.00, TRUE, 'Standard dal cooking base (~185 kcal)'),
(38, '1/2 cup raw dal', 100.00, FALSE, 'Family pot dal base (~370 kcal)');


-- Food 39: Kabuli Chana / Chickpeas, white whole, raw (IFCT B008)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (39, 'IFCT_B008', 'IFCT_2017', 'B008', 'Kabuli Chana / White Chickpeas, raw', 'Cicer arietinum', 'Grain Legumes and Pulses', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (39, 'PER_100G', 362.10, 1515.00, 20.47, 57.43, 5.48, 17.40, 9.85, 2.80, '1515 kJ', 'SOURCE_VERIFIED', 'Whole dry white chickpeas for chole and salads. Source energy 1515 kJ (362.1 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(39, '1/4 cup raw chickpeas', 50.00, TRUE, 'Single portion raw chole measure (~181 kcal)'),
(39, '1/2 cup raw chickpeas', 100.00, FALSE, 'Standard recipe cooking base (~362 kcal)');


-- Food 40: Rajma / Red Kidney Beans, raw (IFCT B018)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (40, 'IFCT_B018', 'IFCT_2017', 'B018', 'Rajma / Red Kidney Beans, raw', 'Phaseolus vulgaris', 'Grain Legumes and Pulses', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (40, 'PER_100G', 346.60, 1450.00, 22.90, 58.60, 1.30, 24.90, 9.60, 3.20, '1450 kJ', 'SOURCE_VERIFIED', 'Whole dry red kidney beans for traditional curry. Source energy 1450 kJ (346.6 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(40, '1/4 cup raw beans', 50.00, TRUE, 'Standard dry rajma portion (~173 kcal)'),
(40, '1/2 cup raw beans', 100.00, FALSE, 'Full curry recipe base (~347 kcal)');


-- Food 41: Ginger, fresh raw / Adrak (IFCT G010)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (41, 'IFCT_G010', 'IFCT_2017', 'G010', 'Ginger, fresh raw (Adrak)', 'Zingiber officinale', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (41, 'PER_100G', 55.00, 230.00, 1.82, 10.35, 0.75, 2.40, 80.89, 1.20, '230 kJ', 'SOURCE_VERIFIED', 'Fresh pungent aromatic rhizome. Source energy 230 kJ (55 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(41, '1 tsp grated ginger', 5.00, TRUE, 'Seasoning / chai measure (~3 kcal)'),
(41, '1 inch root piece', 10.00, FALSE, 'Cooking knob (~6 kcal)'),
(41, '1 tbsp minced ginger', 15.00, FALSE, 'Curry paste measure (~8 kcal)');


-- Food 42: Garlic, fresh raw / Lahsun (IFCT G009)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (42, 'IFCT_G009', 'IFCT_2017', 'G009', 'Garlic, fresh raw (Lahsun)', 'Allium sativum', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (42, 'PER_100G', 125.00, 523.00, 6.30, 22.80, 0.50, 2.10, 62.80, 1.50, '523 kJ', 'SOURCE_VERIFIED', 'Fresh garlic bulb cloves. Source energy 523 kJ (125 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(42, '1 clove', 3.00, TRUE, 'Single cooking clove (~4 kcal)'),
(42, '3 cloves', 9.00, FALSE, 'Typical dish seasoning (~11 kcal)'),
(42, '1 tbsp minced garlic', 12.00, FALSE, 'Generous curry measure (~15 kcal)');


-- Food 43: Curry Leaves, fresh / Kadi Patta (IFCT C009)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (43, 'IFCT_C009', 'IFCT_2017', 'C009', 'Curry Leaves, fresh (Kadi Patta)', 'Murraya koenigii', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (43, 'PER_100G', 108.00, 452.00, 6.10, 15.20, 1.00, 6.40, 63.80, 4.20, '452 kJ', 'SOURCE_VERIFIED', 'Aromatic fresh green curry foliage for tempering. Source energy 452 kJ (108 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(43, '1 sprig (10-12 leaves)', 3.00, TRUE, 'Tadka tempering sprig (~3 kcal)'),
(43, '1 tbsp chopped leaves', 6.00, FALSE, 'Chutney / sambar seasoning (~6 kcal)');


-- Food 44: Coriander Leaves, fresh / Hara Dhaniya (IFCT C008)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (44, 'IFCT_C008', 'IFCT_2017', 'C008', 'Coriander Leaves, fresh (Hara Dhaniya)', 'Coriandrum sativum', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (44, 'PER_100G', 31.10, 130.00, 3.30, 2.40, 0.60, 4.20, 87.90, 1.60, '130 kJ', 'SOURCE_VERIFIED', 'Fresh green cilantro herbs for garnish and chutney. Source energy 130 kJ (31.1 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(44, '1 tbsp chopped garnish', 5.00, TRUE, 'Dish garnish pinch (~1.5 kcal)'),
(44, '1/4 cup chopped leaves', 15.00, FALSE, 'Chutney base (~5 kcal)');


-- Food 45: Carrot, orange/red fresh raw / Gajar (IFCT D014)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (45, 'IFCT_D014', 'IFCT_2017', 'D014', 'Carrot, fresh raw (Gajar)', 'Daucus carota', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (45, 'PER_100G', 32.50, 136.00, 0.90, 6.40, 0.20, 2.80, 88.80, 0.90, '136 kJ', 'SOURCE_VERIFIED', 'Crisp fresh raw garden carrot roots. Source energy 136 kJ (32.5 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(45, '1 medium carrot', 80.00, TRUE, 'Standard domestic cooking carrot (~26 kcal)'),
(45, '1 small carrot', 50.00, FALSE, 'Small snack carrot (~16 kcal)'),
(45, '1 cup chopped', 120.00, FALSE, 'Salad / sabzi bowl (~39 kcal)');


-- Food 46: Brinjal / Eggplant, fresh raw / Baingan (IFCT D008)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (46, 'IFCT_D008', 'IFCT_2017', 'D008', 'Brinjal / Eggplant, fresh raw (Baingan)', 'Solanum melongena', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (46, 'PER_100G', 25.10, 105.00, 1.40, 3.80, 0.30, 3.40, 91.50, 0.60, '105 kJ', 'SOURCE_VERIFIED', 'Fresh raw purple eggplants for bharta, sambar, and curries. Source energy 105 kJ (25.1 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(46, '1 small brinjal', 70.00, TRUE, 'Small round brinjal (~18 kcal)'),
(46, '1 medium brinjal', 150.00, FALSE, 'Large roasting brinjal (~38 kcal)');


-- Food 47: Spinach / Palak, fresh raw (IFCT C019)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (47, 'IFCT_C019', 'IFCT_2017', 'C019', 'Spinach / Palak, fresh raw leaves', 'Spinacia oleracea', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (47, 'PER_100G', 26.10, 109.00, 2.00, 2.90, 0.70, 2.20, 91.60, 1.60, '109 kJ', 'SOURCE_VERIFIED', 'Fresh raw leafy green spinach before cooking. Source energy 109 kJ (26.1 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(47, '1 cup chopped raw leaves', 30.00, TRUE, 'Fresh raw salad measure (~8 kcal)'),
(47, '1 bunch trimmed leaves', 250.00, FALSE, 'Standard market bunch before cooking (~65 kcal)');


-- Food 48: Potato, fresh raw / Aloo (IFCT E010)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (48, 'IFCT_E010', 'IFCT_2017', 'E010', 'Potato, fresh raw (Aloo)', 'Solanum tuberosum', 'Other Vegetables', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (48, 'PER_100G', 70.00, 293.00, 1.60, 15.20, 0.10, 1.70, 80.60, 0.80, '293 kJ', 'SOURCE_VERIFIED', 'Fresh raw unpeeled potato tubers. Source energy 293 kJ (70 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(48, '1 medium potato (raw)', 120.00, TRUE, 'Standard domestic potato (~84 kcal)'),
(48, '1 small potato (raw)', 75.00, FALSE, 'Small recipe potato (~53 kcal)');


-- Food 49: Coconut Kernel, fresh raw / Nariyal (IFCT F012)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (49, 'IFCT_F012', 'IFCT_2017', 'F012', 'Coconut Kernel, fresh raw (Nariyal)', 'Cocos nucifera', 'Nuts and Seeds', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (49, 'PER_100G', 354.00, 1481.00, 3.33, 7.25, 33.49, 9.00, 45.00, 1.00, '1481 kJ', 'SOURCE_VERIFIED', 'Fresh moist mature white coconut meat kernel. Source energy 1481 kJ (354 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(49, '1 tbsp grated coconut', 10.00, TRUE, 'Garnish / tempering spoon (~35 kcal)'),
(49, '1 piece / chunk', 25.00, FALSE, 'Snack bite chunk (~89 kcal)'),
(49, '1/4 cup grated coconut', 30.00, FALSE, 'Chutney base measure (~106 kcal)');


-- Food 50: Lemon, fresh raw / Nimbu (IFCT F023)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (50, 'IFCT_F023', 'IFCT_2017', 'F023', 'Lemon / Lime, fresh raw (Nimbu)', 'Citrus aurantifolia', 'Fruits', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (50, 'PER_100G', 27.00, 113.00, 0.70, 5.70, 0.10, 1.30, 91.80, 0.40, '113 kJ', 'SOURCE_VERIFIED', 'Fresh citrus lemon/lime juice and pulp. Source energy 113 kJ (27 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(50, '1 tsp fresh lemon juice', 5.00, TRUE, 'Seasoning squeeze (~1.4 kcal)'),
(50, '1/2 lemon juice squeeze', 15.00, FALSE, 'Dish seasoning squeeze (~4 kcal)'),
(50, '1 medium whole lemon', 55.00, FALSE, 'Full fresh lemon fruit (~15 kcal)');


-- Food 51: Tamarind, raw pulp / Imli (IFCT G031)
INSERT INTO foods (id, food_code, source_id, source_record_id, name, scientific_name, food_group, food_state, brand_name, barcode, is_verified, is_active)
VALUES (51, 'IFCT_G031', 'IFCT_2017', 'G031', 'Tamarind, raw pulp (Imli)', 'Tamarindus indica', 'Condiments and Spices', 'RAW_INGREDIENT', NULL, NULL, TRUE, TRUE);

INSERT INTO food_nutrients (food_id, basis, energy_kcal, energy_kj, protein_g, available_carbohydrates_g, total_fat_g, dietary_fiber_g, moisture_water_g, ash_g, raw_source_energy, quality_status, correction_notes)
VALUES (51, 'PER_100G', 240.00, 1004.00, 3.10, 56.70, 0.10, 5.60, 28.20, 2.10, '1004 kJ', 'SOURCE_VERIFIED', 'Tart sticky brown tamarind pulp for rasam, sambar, and chutney. Source energy 1004 kJ (240 kcal).');

INSERT INTO food_portions (food_id, portion_description, gram_weight, is_default_portion, source_note)
VALUES 
(51, '1 small ball (marble-sized)', 5.00, TRUE, 'Single dish souring measure (~12 kcal)'),
(51, '1 tablespoon (tbsp) pulp', 15.00, FALSE, 'Generous rasam / sambar measure (~36 kcal)');


-- ==============================================================================
-- 3. Comprehensive Indian Food Aliases Seed (Common, Regional, and Transliterations)
-- ==============================================================================

-- Existing Foods 1-33
-- Food 1: Rice, white, long-grain, regular, raw
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(1, 'white rice', 'ENGLISH', TRUE),
(1, 'raw rice', 'ENGLISH', FALSE),
(1, 'chawal', 'HINDI', TRUE),
(1, 'raw chawal', 'HINDI', FALSE),
(1, 'arisi', 'TAMIL', FALSE),
(1, 'biyyam', 'TELUGU', FALSE);

-- Food 2: Rice, white, cooked, regular
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(2, 'cooked rice', 'ENGLISH', TRUE),
(2, 'boiled rice', 'ENGLISH', FALSE),
(2, 'steamed rice', 'ENGLISH', FALSE),
(2, 'bhat', 'HINDI', TRUE),
(2, 'sadham', 'TAMIL', FALSE),
(2, 'annam', 'TELUGU', FALSE);

-- Food 3: Red gram, dal (Toor dal / Arhar dal)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(3, 'toor dal', 'COMMON', TRUE),
(3, 'tuvar dal', 'REGIONAL', FALSE),
(3, 'arhar dal', 'HINDI', TRUE),
(3, 'pigeon pea', 'ENGLISH', FALSE),
(3, 'sambar paruppu', 'TAMIL', FALSE),
(3, 'kandi pappu', 'TELUGU', FALSE),
(3, 'togari bele', 'KANNADA', FALSE);

-- Food 4: Ghee (Clarified Butter)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(4, 'ghee', 'COMMON', TRUE),
(4, 'desi ghee', 'HINDI', TRUE),
(4, 'clarified butter', 'ENGLISH', FALSE),
(4, 'neyyi', 'TELUGU', FALSE),
(4, 'nei', 'TAMIL', FALSE),
(4, 'tuppa', 'KANNADA', FALSE);

-- Food 5: Paneer (Fresh Cottage Cheese)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(5, 'paneer', 'COMMON', TRUE),
(5, 'cottage cheese', 'ENGLISH', FALSE),
(5, 'panir', 'COMMON', FALSE),
(5, 'chenna', 'REGIONAL', FALSE);

-- Food 6: Amul Pasteurized Butter
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(6, 'butter', 'ENGLISH', TRUE),
(6, 'makkhan', 'HINDI', TRUE),
(6, 'makhan', 'HINDI', FALSE),
(6, 'amul butter', 'BRAND', FALSE),
(6, 'vennai', 'TAMIL', FALSE),
(6, 'benne', 'KANNADA', FALSE);

-- Food 7: Wheat flour, whole (Atta)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(7, 'atta', 'HINDI', TRUE),
(7, 'gehun ka atta', 'HINDI', FALSE),
(7, 'whole wheat flour', 'ENGLISH', TRUE),
(7, 'wheat flour', 'ENGLISH', FALSE),
(7, 'godhumai maavu', 'TAMIL', FALSE),
(7, 'godhuma pindi', 'TELUGU', FALSE);

-- Food 8: Black gram, dal (Urad dal)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(8, 'urad dal', 'COMMON', TRUE),
(8, 'black gram dal', 'ENGLISH', FALSE),
(8, 'ulutham paruppu', 'TAMIL', FALSE),
(8, 'minapa pappu', 'TELUGU', FALSE),
(8, 'uddina bele', 'KANNADA', FALSE);

-- Food 9: Chapati / Phulka
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(9, 'chapati', 'COMMON', TRUE),
(9, 'roti', 'HINDI', TRUE),
(9, 'phulka', 'HINDI', FALSE),
(9, 'rotli', 'GUJARATI', FALSE),
(9, 'chappathi', 'TAMIL', FALSE);

-- Food 10: Steamed Idli
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(10, 'idli', 'COMMON', TRUE),
(10, 'idly', 'COMMON', FALSE),
(10, 'steamed idli', 'ENGLISH', FALSE);

-- Food 11: Dal Tadka
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(11, 'dal tadka', 'COMMON', TRUE),
(11, 'dal fry', 'COMMON', FALSE),
(11, 'yellow dal', 'ENGLISH', FALSE),
(11, 'tempered dal', 'ENGLISH', FALSE);

-- Food 12: Basmati Rice, cooked
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(12, 'basmati', 'COMMON', TRUE),
(12, 'basmati rice', 'ENGLISH', TRUE),
(12, 'cooked basmati', 'ENGLISH', FALSE),
(12, 'steamed basmati', 'ENGLISH', FALSE);

-- Food 13: Plain Sada Dosa
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(13, 'dosa', 'COMMON', TRUE),
(13, 'dosai', 'TAMIL', FALSE),
(13, 'sada dosa', 'COMMON', FALSE),
(13, 'plain dosa', 'ENGLISH', FALSE);

-- Food 14: Vegetable Sambar
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(14, 'sambar', 'COMMON', TRUE),
(14, 'sambaar', 'COMMON', FALSE),
(14, 'veg sambar', 'ENGLISH', FALSE),
(14, 'vegetable sambar', 'ENGLISH', FALSE);

-- Food 15: Green gram dal / Moong dal, split, raw
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(15, 'moong dal', 'COMMON', TRUE),
(15, 'mung dal', 'COMMON', FALSE),
(15, 'green gram dal', 'ENGLISH', FALSE),
(15, 'paasi paruppu', 'TAMIL', FALSE),
(15, 'pesara pappu', 'TELUGU', FALSE),
(15, 'hesaru bele', 'KANNADA', FALSE);

-- Food 16: Moong Dal, cooked plain
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(16, 'cooked moong dal', 'ENGLISH', TRUE),
(16, 'boiled moong dal', 'ENGLISH', FALSE),
(16, 'cooked mung dal', 'ENGLISH', FALSE);

-- Food 17: Curd / Dahi (Cow Milk)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(17, 'dahi', 'HINDI', TRUE),
(17, 'curd', 'ENGLISH', TRUE),
(17, 'yogurt', 'ENGLISH', FALSE),
(17, 'thayir', 'TAMIL', FALSE),
(17, 'perugu', 'TELUGU', FALSE),
(17, 'mosaru', 'KANNADA', FALSE),
(17, 'dohi', 'REGIONAL', FALSE);

-- Food 18: Cow Milk, whole fresh
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(18, 'doodh', 'HINDI', TRUE),
(18, 'milk', 'ENGLISH', TRUE),
(18, 'cow milk', 'ENGLISH', FALSE),
(18, 'paal', 'TAMIL', FALSE),
(18, 'palu', 'TELUGU', FALSE),
(18, 'haalu', 'KANNADA', FALSE);

-- Food 19: Egg, whole, boiled
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(19, 'anda', 'HINDI', TRUE),
(19, 'egg', 'ENGLISH', TRUE),
(19, 'boiled egg', 'ENGLISH', FALSE),
(19, 'muttai', 'TAMIL', FALSE),
(19, 'guddu', 'TELUGU', FALSE),
(19, 'motte', 'KANNADA', FALSE);

-- Food 20: Chicken breast, meat only, cooked
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(20, 'chicken', 'ENGLISH', TRUE),
(20, 'chicken breast', 'ENGLISH', FALSE),
(20, 'murgh', 'HINDI', TRUE),
(20, 'kodi kura', 'TELUGU', FALSE),
(20, 'kozhi', 'TAMIL', FALSE);

-- Food 21: Rohu Fish, cooked
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(21, 'fish', 'ENGLISH', TRUE),
(21, 'machli', 'HINDI', TRUE),
(21, 'rohu', 'COMMON', FALSE),
(21, 'meen', 'TAMIL', FALSE),
(21, 'chepa', 'TELUGU', FALSE);

-- Food 22: Spinach / Palak, steamed / cooked
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(22, 'cooked palak', 'COMMON', TRUE),
(22, 'cooked spinach', 'ENGLISH', FALSE),
(22, 'palak sabzi', 'HINDI', FALSE);

-- Food 23: Potato (Aloo), boiled, peeled
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(23, 'boiled aloo', 'HINDI', TRUE),
(23, 'boiled potato', 'ENGLISH', FALSE),
(23, 'urulaikizhangu', 'TAMIL', FALSE);

-- Food 24: Banana (Robusta / Ripe)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(24, 'kela', 'HINDI', TRUE),
(24, 'banana', 'ENGLISH', TRUE),
(24, 'vazhaipazham', 'TAMIL', FALSE),
(24, 'arati pandu', 'TELUGU', FALSE),
(24, 'bale hannu', 'KANNADA', FALSE);

-- Food 25: Apple, fresh with skin
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(25, 'apple', 'ENGLISH', TRUE),
(25, 'seb', 'HINDI', TRUE);

-- Food 26: Poha (Cooked Seasoned Flattened Rice)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(26, 'cooked poha', 'COMMON', TRUE),
(26, 'kanda poha', 'MARATHI', FALSE),
(26, 'batata poha', 'MARATHI', FALSE),
(26, 'aval upma', 'TAMIL', FALSE);

-- Food 27: Semolina / Suji / Rava
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(27, 'suji', 'HINDI', TRUE),
(27, 'sooji', 'HINDI', FALSE),
(27, 'rava', 'REGIONAL', TRUE),
(27, 'semolina', 'ENGLISH', TRUE),
(27, 'cream of wheat', 'ENGLISH', FALSE),
(27, 'bombay rava', 'REGIONAL', FALSE);

-- Food 28: Onion, fresh raw
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(28, 'pyaaz', 'HINDI', TRUE),
(28, 'pyaj', 'HINDI', FALSE),
(28, 'onion', 'ENGLISH', TRUE),
(28, 'kanda', 'MARATHI', FALSE),
(28, 'ulli', 'MALAYALAM', FALSE),
(28, 'savala', 'MALAYALAM', FALSE),
(28, 'vengayam', 'TAMIL', FALSE),
(28, 'eerulli', 'KANNADA', FALSE);

-- Food 29: Tomato, ripe raw
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(29, 'tamatar', 'HINDI', TRUE),
(29, 'tomato', 'ENGLISH', TRUE),
(29, 'thakkali', 'TAMIL', FALSE),
(29, 'tameta', 'GUJARATI', FALSE);

-- Food 30: Green Chili, fresh raw
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(30, 'hari mirch', 'HINDI', TRUE),
(30, 'green chili', 'ENGLISH', TRUE),
(30, 'green chilli', 'ENGLISH', FALSE),
(30, 'mirchi', 'COMMON', FALSE),
(30, 'pacha mulaku', 'MALAYALAM', FALSE);

-- Food 31: Cumin Seeds (Jeera)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(31, 'jeera', 'HINDI', TRUE),
(31, 'jira', 'HINDI', FALSE),
(31, 'cumin', 'ENGLISH', TRUE),
(31, 'cumin seeds', 'ENGLISH', FALSE),
(31, 'zeera', 'COMMON', FALSE),
(31, 'seeragam', 'TAMIL', FALSE),
(31, 'jeelakarra', 'TELUGU', FALSE);

-- Food 32: Turmeric Powder (Haldi)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(32, 'haldi', 'HINDI', TRUE),
(32, 'turmeric', 'ENGLISH', TRUE),
(32, 'turmeric powder', 'ENGLISH', FALSE),
(32, 'manjal', 'TAMIL', FALSE),
(32, 'pasupu', 'TELUGU', FALSE);

-- Food 33: Mustard Oil, cold-pressed
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(33, 'mustard oil', 'ENGLISH', TRUE),
(33, 'sarson ka tel', 'HINDI', TRUE),
(33, 'sarson tel', 'HINDI', FALSE),
(33, 'kadugu ennai', 'TAMIL', FALSE);

-- Expanded Foods 34-51 Aliases
-- Food 34: Brown Rice
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(34, 'brown rice', 'ENGLISH', TRUE),
(34, 'brown chawal', 'HINDI', TRUE),
(34, 'unpolished rice', 'ENGLISH', FALSE);

-- Food 35: Rolled Oats
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(35, 'oats', 'ENGLISH', TRUE),
(35, 'rolled oats', 'ENGLISH', FALSE),
(35, 'oatmeal', 'ENGLISH', FALSE),
(35, 'jaee', 'HINDI', FALSE);

-- Food 36: Besan
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(36, 'besan', 'HINDI', TRUE),
(36, 'gram flour', 'ENGLISH', TRUE),
(36, 'chickpea flour', 'ENGLISH', FALSE),
(36, 'kadala maavu', 'TAMIL', FALSE),
(36, 'senaga pindi', 'TELUGU', FALSE);

-- Food 37: Raw Poha
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(37, 'poha', 'HINDI', TRUE),
(37, 'aval', 'TAMIL', FALSE),
(37, 'avalakki', 'KANNADA', FALSE),
(37, 'flattened rice', 'ENGLISH', FALSE),
(37, 'beaten rice', 'ENGLISH', FALSE),
(37, 'atukulu', 'TELUGU', FALSE);

-- Food 38: Chana Dal
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(38, 'chana dal', 'HINDI', TRUE),
(38, 'split bengal gram', 'ENGLISH', FALSE),
(38, 'senaga pappu', 'TELUGU', FALSE),
(38, 'kadalai paruppu', 'TAMIL', FALSE),
(38, 'kadale bele', 'KANNADA', FALSE);

-- Food 39: Kabuli Chana
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(39, 'chana', 'COMMON', TRUE),
(39, 'kabuli chana', 'HINDI', TRUE),
(39, 'chickpeas', 'ENGLISH', TRUE),
(39, 'chole', 'HINDI', FALSE),
(39, 'safed chana', 'HINDI', FALSE),
(39, 'white chana', 'ENGLISH', FALSE),
(39, 'kondakadalai', 'TAMIL', FALSE);

-- Food 40: Rajma
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(40, 'rajma', 'HINDI', TRUE),
(40, 'rajmah', 'HINDI', FALSE),
(40, 'kidney beans', 'ENGLISH', TRUE),
(40, 'red kidney beans', 'ENGLISH', FALSE);

-- Food 41: Ginger, fresh (Adrak)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(41, 'adrak', 'HINDI', TRUE),
(41, 'ginger', 'ENGLISH', TRUE),
(41, 'inji', 'TAMIL', FALSE),
(41, 'allam', 'TELUGU', FALSE),
(41, 'shunti', 'KANNADA', FALSE);

-- Food 42: Garlic, fresh (Lahsun)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(42, 'lahsun', 'HINDI', TRUE),
(42, 'lasun', 'HINDI', FALSE),
(42, 'garlic', 'ENGLISH', TRUE),
(42, 'poondu', 'TAMIL', FALSE),
(42, 'vellulli', 'TELUGU', FALSE),
(42, 'bellulli', 'KANNADA', FALSE);

-- Food 43: Curry Leaves (Kadi Patta)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(43, 'kadi patta', 'HINDI', TRUE),
(43, 'curry leaves', 'ENGLISH', TRUE),
(43, 'karivepaku', 'TELUGU', FALSE),
(43, 'karuveppilai', 'TAMIL', FALSE),
(43, 'meetha neem', 'HINDI', FALSE);

-- Food 44: Coriander Leaves (Hara Dhaniya)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(44, 'dhaniya', 'HINDI', TRUE),
(44, 'hara dhaniya', 'HINDI', FALSE),
(44, 'coriander', 'ENGLISH', TRUE),
(44, 'coriander leaves', 'ENGLISH', FALSE),
(44, 'cilantro', 'ENGLISH', FALSE),
(44, 'koththamalli', 'TAMIL', FALSE),
(44, 'kothimeera', 'TELUGU', FALSE);

-- Food 45: Carrot, fresh (Gajar)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(45, 'gajar', 'HINDI', TRUE),
(45, 'carrot', 'ENGLISH', TRUE),
(45, 'orange carrot', 'ENGLISH', FALSE);

-- Food 46: Brinjal / Eggplant (Baingan)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(46, 'baingan', 'HINDI', TRUE),
(46, 'brinjal', 'ENGLISH', TRUE),
(46, 'eggplant', 'ENGLISH', FALSE),
(46, 'vankaya', 'TELUGU', FALSE),
(46, 'kathirikai', 'TAMIL', FALSE);

-- Food 47: Spinach, fresh raw (Palak)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(47, 'palak', 'HINDI', TRUE),
(47, 'spinach', 'ENGLISH', TRUE),
(47, 'keerai', 'TAMIL', FALSE),
(47, 'palakura', 'TELUGU', FALSE);

-- Food 48: Potato, fresh raw (Aloo)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(48, 'aloo', 'HINDI', TRUE),
(48, 'alu', 'HINDI', FALSE),
(48, 'potato', 'ENGLISH', TRUE),
(48, 'batata', 'MARATHI', FALSE),
(48, 'urulaikizhangu', 'TAMIL', FALSE),
(48, 'bangaladumpa', 'TELUGU', FALSE);

-- Food 49: Coconut Kernel, fresh (Nariyal)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(49, 'nariyal', 'HINDI', TRUE),
(49, 'coconut', 'ENGLISH', TRUE),
(49, 'fresh coconut', 'ENGLISH', FALSE),
(49, 'thengai', 'TAMIL', FALSE),
(49, 'kobbari', 'TELUGU', FALSE),
(49, 'tenginkai', 'KANNADA', FALSE);

-- Food 50: Lemon / Lime (Nimbu)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(50, 'nimbu', 'HINDI', TRUE),
(50, 'lemon', 'ENGLISH', TRUE),
(50, 'lime', 'ENGLISH', FALSE),
(50, 'elamichai', 'TAMIL', FALSE),
(50, 'nimma pandu', 'TELUGU', FALSE);

-- Food 51: Tamarind, pulp (Imli)
INSERT INTO food_aliases (food_id, alias_name, language_or_dialect, is_primary) VALUES
(51, 'imli', 'HINDI', TRUE),
(51, 'tamarind', 'ENGLISH', TRUE),
(51, 'puli', 'TAMIL', FALSE),
(51, 'chintha pandu', 'TELUGU', FALSE),
(51, 'hunasehannu', 'KANNADA', FALSE);
