-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V3
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: Persistent Meal Logging, Nutrient Snapshots, and User Target Settings
-- ==============================================================================

-- 1. User Targets Table
CREATE TABLE IF NOT EXISTS user_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    daily_calories INT NOT NULL DEFAULT 2000,
    protein_g DECIMAL(6, 1) NOT NULL DEFAULT 75.0,
    carbs_g DECIMAL(6, 1) NOT NULL DEFAULT 250.0,
    fat_g DECIMAL(6, 1) NOT NULL DEFAULT 65.0,
    fiber_g DECIMAL(6, 1) NOT NULL DEFAULT 30.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_targets_user ON user_targets(user_id);

-- Insert default user target
INSERT INTO user_targets (user_id, daily_calories, protein_g, carbs_g, fat_g, fiber_g)
VALUES ('default-user', 2000, 75.0, 250.0, 65.0, 30.0);

-- 2. Meal Logs Table (One record per user per date)
CREATE TABLE IF NOT EXISTS meal_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    log_date DATE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_meal_logs_user_date UNIQUE (user_id, log_date)
);

CREATE INDEX idx_meal_logs_user_date ON meal_logs(user_id, log_date);

-- 3. Meal Entries Table (Multiple food or recipe entries per meal)
CREATE TABLE IF NOT EXISTS meal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_log_id BIGINT NOT NULL,
    meal_type VARCHAR(30) NOT NULL, -- BREAKFAST, LUNCH, DINNER, SNACKS
    food_id BIGINT,
    recipe_id BIGINT,
    food_name VARCHAR(255) NOT NULL,
    food_state VARCHAR(50) NOT NULL,
    portion_description VARCHAR(150) NOT NULL,
    portion_gram_weight DECIMAL(8, 2) NOT NULL,
    quantity_multiplier DECIMAL(6, 2) NOT NULL DEFAULT 1.00,
    total_gram_weight DECIMAL(8, 2) NOT NULL,
    energy_kcal DECIMAL(8, 2) NOT NULL,
    energy_kj DECIMAL(8, 2),
    protein_g DECIMAL(8, 2) NOT NULL,
    carbohydrates_g DECIMAL(8, 2) NOT NULL,
    fat_g DECIMAL(8, 2) NOT NULL,
    fiber_g DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entries_log FOREIGN KEY (meal_log_id) REFERENCES meal_logs(id) ON DELETE CASCADE,
    CONSTRAINT fk_entries_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE SET NULL,
    CONSTRAINT fk_entries_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE SET NULL
);

CREATE INDEX idx_entries_log_id ON meal_entries(meal_log_id);
CREATE INDEX idx_entries_meal_type ON meal_entries(meal_type);
