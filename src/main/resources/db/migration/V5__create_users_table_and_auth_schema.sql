-- ==============================================================================
-- FitKit Nutrition Data Schema Migration: V5
-- Target Database: MySQL 8.0 & H2 Test Mode
-- Purpose: User Authentication, Credential Storage, and Ownership Isolation
-- ==============================================================================

-- 1. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- 2. Seed Default Development User (Email: demo@fitkit.local, Pass: FitKit2026!)
INSERT INTO users (email, password_hash, full_name, role, is_active)
VALUES (
    'demo@fitkit.local',
    '$2a$10$e882uSg/zM.d62iUv9LgS.P8UsmvY6h6N0aE3i391P6u7bJ8vL4G6',
    'Demo Athlete',
    'ROLE_USER',
    TRUE
);

-- 3. Safely reassign legacy development records from 'default-user' to 'demo@fitkit.local'
UPDATE user_targets SET user_id = 'demo@fitkit.local' WHERE user_id = 'default-user';
UPDATE meal_logs SET user_id = 'demo@fitkit.local' WHERE user_id = 'default-user';
