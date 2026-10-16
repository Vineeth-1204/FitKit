-- Persist the questionnaire used to calculate a new user's personal estimates.
-- Existing accounts deliberately have no profile row and complete onboarding at next sign-in.
CREATE TABLE nutrition_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    age INT NOT NULL,
    sex_for_estimate VARCHAR(20) NOT NULL,
    height_cm INT NOT NULL,
    weight_kg DOUBLE NOT NULL,
    activity_level VARCHAR(20) NOT NULL,
    goal VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_profiles_user FOREIGN KEY (user_id) REFERENCES users(email) ON DELETE CASCADE
);
