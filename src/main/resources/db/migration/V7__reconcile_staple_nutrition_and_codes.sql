-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V7
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: Reconcile Staple Ingredient Nutrition Data & IFCT 2017 Source Codes (Phase 6.1)
-- ==============================================================================

-- 1. Semolina / Suji / Rava (Reconcile to IFCT 2017 Entry A022)
UPDATE foods 
SET food_code = 'IFCT_A022', 
    source_record_id = 'A022', 
    scientific_name = 'Triticum aestivum', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 27;

UPDATE food_nutrients 
SET energy_kcal = 333.70, 
    energy_kj = 1396.00, 
    protein_g = 11.38, 
    available_carbohydrates_g = 68.43, 
    total_fat_g = 0.80, 
    dietary_fiber_g = 9.72, 
    moisture_water_g = 8.94, 
    ash_g = 0.73, 
    raw_source_energy = '1396 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry A022 (Triticum aestivum, semolina/suji). Source energy 1396 kJ (333.7 kcal). Atwater estimate: 326.4 kcal.'
WHERE food_id = 27;

UPDATE food_portions 
SET source_note = 'Typical dry upma recipe base (~300 kcal)' 
WHERE food_id = 27 AND portion_description = '1/2 cup (dry measure)';

UPDATE food_portions 
SET source_note = 'Large recipe measure (~601 kcal)' 
WHERE food_id = 27 AND portion_description = '1 cup (dry measure)';


-- 2. Fresh Onion, raw (Reconcile to IFCT 2017 Entry G017, Big Onion)
UPDATE foods 
SET food_code = 'IFCT_G017', 
    source_record_id = 'G017', 
    scientific_name = 'Allium cepa', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 28;

UPDATE food_nutrients 
SET energy_kcal = 48.50, 
    energy_kj = 203.00, 
    protein_g = 1.20, 
    available_carbohydrates_g = 9.30, 
    total_fat_g = 0.40, 
    dietary_fiber_g = 2.50, 
    moisture_water_g = 84.30, 
    ash_g = 0.80, 
    raw_source_energy = '203 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G017 (Allium cepa, big onion). Source energy 203 kJ (48.5 kcal). Atwater estimate: 45.6 kcal.'
WHERE food_id = 28;

UPDATE food_portions 
SET source_note = 'Standard domestic cooking onion (~34 kcal)' 
WHERE food_id = 28 AND portion_description = '1 medium onion';

UPDATE food_portions 
SET source_note = 'Small garnish onion (~22 kcal)' 
WHERE food_id = 28 AND portion_description = '1 small onion';


-- 3. Fresh Tomato, ripe raw (Reconcile to IFCT 2017 Entry D075, Ripe Hybrid Tomato)
UPDATE foods 
SET food_code = 'IFCT_D075', 
    source_record_id = 'D075', 
    scientific_name = 'Solanum lycopersicum', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 29;

UPDATE food_nutrients 
SET energy_kcal = 20.80, 
    energy_kj = 87.00, 
    protein_g = 0.90, 
    available_carbohydrates_g = 3.60, 
    total_fat_g = 0.20, 
    dietary_fiber_g = 1.70, 
    moisture_water_g = 93.10, 
    ash_g = 0.50, 
    raw_source_energy = '87 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry D075 (Solanum lycopersicum, ripe hybrid tomato). Source energy 87 kJ (20.8 kcal). Atwater estimate: 19.8 kcal.'
WHERE food_id = 29;

UPDATE food_portions 
SET source_note = 'Standard domestic cooking tomato (~19 kcal)' 
WHERE food_id = 29 AND portion_description = '1 medium tomato';

UPDATE food_portions 
SET source_note = 'Small salad tomato (~12 kcal)' 
WHERE food_id = 29 AND portion_description = '1 small tomato';


-- 4. Green Chili, fresh raw (Reconcile to IFCT 2017 Entry G011)
UPDATE foods 
SET food_code = 'IFCT_G011', 
    source_record_id = 'G011', 
    scientific_name = 'Capsicum annuum', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 30;

UPDATE food_nutrients 
SET energy_kcal = 45.70, 
    energy_kj = 191.00, 
    protein_g = 2.60, 
    available_carbohydrates_g = 6.80, 
    total_fat_g = 0.60, 
    dietary_fiber_g = 6.80, 
    moisture_water_g = 81.30, 
    ash_g = 0.90, 
    raw_source_energy = '191 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G011 (Capsicum annuum, green chili). Source energy 191 kJ (45.7 kcal). Atwater estimate: 43.0 kcal.'
WHERE food_id = 30;

UPDATE food_portions 
SET source_note = 'Single chili seasoning (~2.3 kcal)' 
WHERE food_id = 30 AND portion_description = '1 small green chili';

UPDATE food_portions 
SET source_note = 'Double chili portion (~4.6 kcal)' 
WHERE food_id = 30 AND portion_description = '2 green chilies';


-- 5. Cumin Seeds / Jeera (Reconcile to IFCT 2017 Entry G025)
UPDATE foods 
SET food_code = 'IFCT_G025', 
    source_record_id = 'G025', 
    scientific_name = 'Cuminum cyminum', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 31;

UPDATE food_nutrients 
SET energy_kcal = 304.50, 
    energy_kj = 1274.00, 
    protein_g = 18.70, 
    available_carbohydrates_g = 36.60, 
    total_fat_g = 15.00, 
    dietary_fiber_g = 22.62, 
    moisture_water_g = 8.10, 
    ash_g = 7.70, 
    raw_source_energy = '1274 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G025 (Cuminum cyminum, cumin seeds). Source energy 1274 kJ (304.5 kcal). Legacy V6 used old NVIF total carbohydrate (375 kcal). Atwater estimate: 356.2 kcal.'
WHERE food_id = 31;

UPDATE food_portions 
SET source_note = 'Standard tempering spoon (~15 kcal)' 
WHERE food_id = 31 AND portion_description = '1 teaspoon (tsp)';

UPDATE food_portions 
SET source_note = 'Large recipe measure (~37 kcal)' 
WHERE food_id = 31 AND portion_description = '1 tablespoon (tbsp)';


-- 6. Turmeric Powder / Haldi (Reconcile to IFCT 2017 Entry G026)
UPDATE foods 
SET food_code = 'IFCT_G026', 
    source_record_id = 'G026', 
    scientific_name = 'Curcuma longa', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 32;

UPDATE food_nutrients 
SET energy_kcal = 280.60, 
    energy_kj = 1174.00, 
    protein_g = 6.30, 
    available_carbohydrates_g = 64.90, 
    total_fat_g = 3.20, 
    dietary_fiber_g = 21.10, 
    moisture_water_g = 11.40, 
    ash_g = 6.80, 
    raw_source_energy = '1174 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G026 (Curcuma longa, turmeric powder). Source energy 1174 kJ (280.6 kcal). Legacy V6 used old NVIF total carbohydrate (349 kcal). Atwater estimate: 313.6 kcal.'
WHERE food_id = 32;

UPDATE food_portions 
SET source_note = 'Typical dish pinch (~6 kcal)' 
WHERE food_id = 32 AND portion_description = '1/2 teaspoon (tsp)';

UPDATE food_portions 
SET source_note = 'Full teaspoon measure (~11 kcal)' 
WHERE food_id = 32 AND portion_description = '1 teaspoon (tsp)';


-- 7. Mustard Oil, cold-pressed (Reconcile Energy & Transparent Quality Provenance for IFCT T017)
UPDATE foods 
SET food_code = 'IFCT_T017', 
    source_record_id = 'T017', 
    scientific_name = 'Brassica juncea', 
    updated_at = CURRENT_TIMESTAMP
WHERE id = 33;

UPDATE food_nutrients 
SET energy_kcal = 884.00, 
    energy_kj = 3700.00, 
    protein_g = 0.00, 
    available_carbohydrates_g = 0.00, 
    total_fat_g = 100.00, 
    dietary_fiber_g = 0.00, 
    moisture_water_g = 0.00, 
    ash_g = 0.00, 
    raw_source_energy = '0 kJ (anomaly)', 
    quality_status = 'CORRECTED_ANOMALY', 
    correction_notes = 'CORRECTION AUDIT: Source recorded 0 kJ despite 100g lipids. Estimated using FAO/INFOODS specific vegetable oil factor 8.84 kcal/g (884.0 kcal / 3700 kJ); standard Atwater 9.0 kcal/g yields 900.0 kcal (3765.6 kJ).'
WHERE food_id = 33;

UPDATE food_portions 
SET source_note = 'Cooking measure (~44 kcal)' 
WHERE food_id = 33 AND portion_description = '1 teaspoon (tsp)';

UPDATE food_portions 
SET source_note = 'Cooking oil measure (~124 kcal)' 
WHERE food_id = 33 AND portion_description = '1 tablespoon (tbsp)';
