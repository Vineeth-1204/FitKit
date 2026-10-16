-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V1
-- Target Database: MySQL 8.0 (ANSI SQL / H2 MySQL Mode compatible)
-- Purpose: Canonical schema for foods, nutrients, portions, recipes, and provenance
-- ==============================================================================

-- 1. Source Provenance Table
CREATE TABLE IF NOT EXISTS source_provenance (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    dataset_name VARCHAR(150) NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    version_or_date VARCHAR(50) NOT NULL,
    license_type VARCHAR(100) NOT NULL,
    official_url VARCHAR(500),
    is_commercial_allowed BOOLEAN DEFAULT FALSE,
    citation_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Food Items Table
CREATE TABLE IF NOT EXISTS foods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_code VARCHAR(100) NOT NULL UNIQUE,
    source_id VARCHAR(50) NOT NULL,
    source_record_id VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    scientific_name VARCHAR(255),
    food_group VARCHAR(100),
    food_state VARCHAR(50) NOT NULL, -- RAW_INGREDIENT, COOKED_SINGLE, PREPARED_RECIPE, PACKAGED_COMMERCIAL
    brand_name VARCHAR(150),
    barcode VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_foods_source FOREIGN KEY (source_id) REFERENCES source_provenance(id)
);

CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_barcode ON foods(barcode);
CREATE INDEX idx_foods_source_record ON foods(source_id, source_record_id);

-- 3. Food Nutrients Table
CREATE TABLE IF NOT EXISTS food_nutrients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    basis VARCHAR(30) NOT NULL DEFAULT 'PER_100G', -- PER_100G, PER_SERVING
    energy_kcal DECIMAL(8, 2),
    energy_kj DECIMAL(8, 2),
    protein_g DECIMAL(8, 2),
    available_carbohydrates_g DECIMAL(8, 2),
    total_fat_g DECIMAL(8, 2),
    dietary_fiber_g DECIMAL(8, 2),
    moisture_water_g DECIMAL(8, 2),
    ash_g DECIMAL(8, 2),
    micronutrients_json TEXT,
    raw_source_energy VARCHAR(50),
    quality_status VARCHAR(50) NOT NULL DEFAULT 'SOURCE_VERIFIED', -- SOURCE_VERIFIED, CALCULATED_ESTIMATE, CORRECTED_ANOMALY, UNVERIFIED
    correction_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrients_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    CONSTRAINT uq_nutrients_food_basis UNIQUE (food_id, basis)
);

CREATE INDEX idx_nutrients_food_id ON food_nutrients(food_id);

-- 4. Food Portions Table
CREATE TABLE IF NOT EXISTS food_portions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    portion_description VARCHAR(150) NOT NULL,
    gram_weight DECIMAL(8, 2) NOT NULL,
    is_default_portion BOOLEAN DEFAULT FALSE,
    source_note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portions_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

CREATE INDEX idx_portions_food_id ON food_portions(food_id);

-- 5. Recipes Table
CREATE TABLE IF NOT EXISTS recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_code VARCHAR(100) NOT NULL UNIQUE,
    food_id BIGINT UNIQUE, -- Optional link to corresponding foods entry representing cooked recipe
    title VARCHAR(255) NOT NULL,
    cuisine_region VARCHAR(100),
    total_raw_weight_g DECIMAL(8, 2) NOT NULL,
    cooking_yield_factor DECIMAL(5, 3) NOT NULL, -- WeightCooked / WeightRaw
    cooked_weight_g DECIMAL(8, 2) NOT NULL,
    servings_yield DECIMAL(5, 2) NOT NULL,
    serving_weight_g DECIMAL(8, 2) NOT NULL,
    cooking_method VARCHAR(50) NOT NULL, -- BOILING, STEAMING, ROASTING_TAWA, SHALLOW_FRY, DEEP_FRY, PRESSURE_COOKING
    preparation_notes TEXT,
    uncertainty_rating VARCHAR(50) NOT NULL DEFAULT 'MODERATE_HOME_VARIANCE', -- LOW_STANDARDIZED, MODERATE_HOME_VARIANCE, HIGH_RESTAURANT_VARIANCE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipes_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE SET NULL
);

-- 6. Recipe Ingredients Table
CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL, -- References raw ingredient food item
    raw_quantity_g DECIMAL(8, 2) NOT NULL,
    unit_measure VARCHAR(50) NOT NULL DEFAULT 'g',
    retention_factor_override DECIMAL(5, 3),
    notes VARCHAR(255),
    CONSTRAINT fk_rec_ing_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_ing_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT uq_recipe_ingredient UNIQUE (recipe_id, food_id)
);

CREATE INDEX idx_recipe_ingredients_recipe ON recipe_ingredients(recipe_id);

-- 7. Import Audit Log Table
CREATE TABLE IF NOT EXISTS import_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id VARCHAR(50) NOT NULL,
    import_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    records_read INT NOT NULL DEFAULT 0,
    records_imported INT NOT NULL DEFAULT 0,
    records_skipped_duplicate INT NOT NULL DEFAULT 0,
    records_corrected INT NOT NULL DEFAULT 0,
    records_rejected_invalid INT NOT NULL DEFAULT 0,
    log_summary TEXT,
    CONSTRAINT fk_audit_source FOREIGN KEY (source_id) REFERENCES source_provenance(id)
);
