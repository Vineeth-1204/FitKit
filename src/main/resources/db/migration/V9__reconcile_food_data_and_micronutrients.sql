-- ==============================================================================
-- Flyway Migration V9: Reconcile Food Classification & Populate Micronutrients
-- ==============================================================================
-- Objective:
-- 1. Reconcile food groups to match authentic IFCT 2017 taxonomy:
--    - Curry Leaves (Food 43, IFCT C009) -> 'Green Leafy Vegetables'
--    - Coriander Leaves (Food 44, IFCT C008) -> 'Green Leafy Vegetables'
--    - Spinach (Food 47, IFCT C019) -> 'Green Leafy Vegetables'
--    - Potato (Food 48, IFCT E010) -> 'Roots and Tubers'
-- 2. Populate analytical micronutrient profiles (calcium, iron, vitamin C, zinc,
--    magnesium, potassium, beta-carotene) from IFCT 2017 into micronutrients_json
--    for catalog staples.
-- ==============================================================================

-- 1. Correct Food Group Classifications (IFCT 2017 Section C & E)
UPDATE foods SET food_group = 'Green Leafy Vegetables', updated_at = CURRENT_TIMESTAMP WHERE id = 43;
UPDATE foods SET food_group = 'Green Leafy Vegetables', updated_at = CURRENT_TIMESTAMP WHERE id = 44;
UPDATE foods SET food_group = 'Green Leafy Vegetables', updated_at = CURRENT_TIMESTAMP WHERE id = 47;
UPDATE foods SET food_group = 'Roots and Tubers', updated_at = CURRENT_TIMESTAMP WHERE id = 48;


-- 2. Populate Micronutrients for Catalog Staples from IFCT 2017
-- Grains & Flours
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":7.49,"iron_mg":0.65,"phosphorus_mg":96.0,"zinc_mg":1.21,"magnesium_mg":32.0}' 
WHERE food_id = 1;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":39.36,"iron_mg":3.97,"phosphorus_mg":315.0,"zinc_mg":2.34,"magnesium_mg":125.0}' 
WHERE food_id = 7;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":16.0,"iron_mg":1.30,"phosphorus_mg":102.0,"zinc_mg":0.85,"magnesium_mg":42.0}' 
WHERE food_id = 27;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":12.0,"iron_mg":1.29,"phosphorus_mg":280.0,"magnesium_mg":110.0,"zinc_mg":1.40}' 
WHERE food_id = 34;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":52.0,"iron_mg":4.72,"phosphorus_mg":410.0,"magnesium_mg":177.0,"zinc_mg":3.97}' 
WHERE food_id = 35;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":56.0,"iron_mg":5.30,"phosphorus_mg":312.0,"magnesium_mg":140.0,"zinc_mg":2.80}' 
WHERE food_id = 36;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":20.0,"iron_mg":2.80,"phosphorus_mg":140.0,"magnesium_mg":45.0,"zinc_mg":1.10}' 
WHERE food_id = 37;

-- Pulses & Legumes
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":73.0,"iron_mg":2.70,"phosphorus_mg":304.0,"zinc_mg":1.80,"magnesium_mg":105.0,"folate_mcg":103.0}' 
WHERE food_id = 3;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":154.0,"iron_mg":3.80,"phosphorus_mg":385.0,"zinc_mg":2.10,"magnesium_mg":130.0,"folate_mcg":132.0}' 
WHERE food_id = 8;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":75.0,"iron_mg":3.90,"phosphorus_mg":345.0,"zinc_mg":2.80,"magnesium_mg":127.0,"folate_mcg":140.0}' 
WHERE food_id = 15;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":56.0,"iron_mg":4.80,"phosphorus_mg":340.0,"magnesium_mg":130.0,"zinc_mg":2.20}' 
WHERE food_id = 38;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":62.0,"iron_mg":6.20,"phosphorus_mg":325.0,"magnesium_mg":125.0,"zinc_mg":2.50}' 
WHERE food_id = 39;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":137.0,"iron_mg":5.10,"phosphorus_mg":406.0,"magnesium_mg":184.0,"zinc_mg":3.20}' 
WHERE food_id = 40;

-- Dairy
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":480.0,"iron_mg":0.20,"phosphorus_mg":310.0,"zinc_mg":2.40,"vitamin_a_mcg":210.0}' 
WHERE food_id = 5;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":149.0,"iron_mg":0.10,"phosphorus_mg":130.0,"zinc_mg":0.80,"potassium_mg":150.0}' 
WHERE food_id = 17;

-- Spices, Condiments & Aromatics
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":931.0,"iron_mg":11.70,"zinc_mg":4.80,"magnesium_mg":366.0,"potassium_mg":1780.0}' 
WHERE food_id = 31;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":168.0,"iron_mg":67.80,"zinc_mg":4.10,"magnesium_mg":208.0,"potassium_mg":2080.0}' 
WHERE food_id = 32;

UPDATE food_nutrients 
SET micronutrients_json = '{"vitamin_e_mg":34.0,"erucic_acid_g":42.0}' 
WHERE food_id = 33;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":16.0,"iron_mg":0.60,"vitamin_c_mg":5.0,"magnesium_mg":43.0,"potassium_mg":415.0}' 
WHERE food_id = 41;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":181.0,"iron_mg":1.70,"vitamin_c_mg":14.0,"magnesium_mg":25.0,"phosphorus_mg":153.0}' 
WHERE food_id = 42;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":74.0,"iron_mg":1.30,"phosphorus_mg":113.0,"magnesium_mg":92.0,"potassium_mg":610.0}' 
WHERE food_id = 51;

-- Vegetables & Herbs
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":46.90,"iron_mg":0.60,"vitamin_c_mg":11.0,"potassium_mg":127.0,"folate_mcg":19.0}' 
WHERE food_id = 28;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":20.0,"iron_mg":0.64,"vitamin_c_mg":27.0,"potassium_mg":146.0,"lycopene_mcg":4500.0}' 
WHERE food_id = 29;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":830.0,"iron_mg":0.93,"vitamin_c_mg":4.0,"magnesium_mg":44.0,"beta_carotene_mcg":7560.0}' 
WHERE food_id = 43;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":184.0,"iron_mg":1.42,"vitamin_c_mg":135.0,"beta_carotene_mcg":4340.0}' 
WHERE food_id = 44;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":34.0,"iron_mg":1.03,"vitamin_c_mg":3.0,"beta_carotene_mcg":4200.0}' 
WHERE food_id = 45;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":18.0,"iron_mg":0.38,"vitamin_c_mg":12.0,"potassium_mg":200.0}' 
WHERE food_id = 46;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":73.0,"iron_mg":1.14,"vitamin_c_mg":28.0,"beta_carotene_mcg":5580.0}' 
WHERE food_id = 47;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":10.0,"iron_mg":0.48,"vitamin_c_mg":17.0,"potassium_mg":350.0}' 
WHERE food_id = 48;

-- Fruits & Nuts
UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":10.0,"iron_mg":1.70,"phosphorus_mg":110.0,"magnesium_mg":32.0,"potassium_mg":356.0}' 
WHERE food_id = 49;

UPDATE food_nutrients 
SET micronutrients_json = '{"calcium_mg":40.0,"iron_mg":0.30,"vitamin_c_mg":39.0,"potassium_mg":138.0}' 
WHERE food_id = 50;
