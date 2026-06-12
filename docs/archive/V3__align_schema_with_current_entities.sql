-- Bridge migration: align existing schema with current JPA entities
-- This migration is additive and keeps original FitLife Standard tables intact.

-- ---------- users ----------
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password VARCHAR(255) NULL AFTER email,
    ADD COLUMN IF NOT EXISTS role VARCHAR(50) NULL AFTER password,
    ADD COLUMN IF NOT EXISTS fit_coin INT NOT NULL DEFAULT 0 AFTER status;

UPDATE users
SET password = password_hash
WHERE password IS NULL AND password_hash IS NOT NULL;

UPDATE users u
JOIN (
    SELECT ur.user_id, MIN(r.code) AS role_code
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    GROUP BY ur.user_id
) x ON x.user_id = u.id
SET u.role = x.role_code
WHERE u.role IS NULL;

-- ---------- members ----------
ALTER TABLE members
    ADD COLUMN IF NOT EXISTS height DOUBLE NULL AFTER avatar_url,
    ADD COLUMN IF NOT EXISTS weight DOUBLE NULL AFTER height;

UPDATE members
SET height = height_cm
WHERE height IS NULL AND height_cm IS NOT NULL;

UPDATE members
SET weight = weight_kg
WHERE weight IS NULL AND weight_kg IS NOT NULL;

-- ---------- health_metrics ----------
CREATE TABLE IF NOT EXISTS health_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    height DOUBLE NOT NULL,
    bmi DOUBLE,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_metrics_member FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_health_metrics_member_recorded (member_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO health_metrics (member_id, weight, height, bmi, recorded_at, created_at)
SELECT bm.member_id, bm.weight_kg, bm.height_cm, bm.bmi, bm.recorded_at, bm.created_at
FROM body_metrics bm
LEFT JOIN health_metrics hm
    ON hm.member_id = bm.member_id
   AND hm.recorded_at = bm.recorded_at
WHERE hm.id IS NULL;

-- ---------- packages ----------
CREATE TABLE IF NOT EXISTS packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    duration_months INT NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    thumbnail_url VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_packages_name (name),
    INDEX idx_packages_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO packages (name, price, duration_months, description, status, thumbnail_url, is_deleted)
SELECT gp.name,
       gp.price,
       CASE
           WHEN gp.duration_days IS NULL OR gp.duration_days <= 0 THEN 1
           ELSE CEIL(gp.duration_days / 30)
       END,
       gp.description,
       gp.status,
       gp.thumbnail_url,
       gp.is_deleted
FROM gym_packages gp
LEFT JOIN packages p ON p.name = gp.name
WHERE p.id IS NULL;

-- ---------- subscriptions ----------
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS package_id BIGINT NULL AFTER member_id;

UPDATE subscriptions
SET package_id = gym_package_id
WHERE package_id IS NULL AND gym_package_id IS NOT NULL;

-- ---------- payments ----------
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_date DATETIME NULL AFTER amount;

UPDATE payments
SET payment_date = paid_at
WHERE payment_date IS NULL AND paid_at IS NOT NULL;

-- ---------- AI / workout bridge tables ----------
ALTER TABLE ai_workout_plans
    ADD COLUMN IF NOT EXISTS plan_data JSON NULL AFTER goal;

CREATE TABLE IF NOT EXISTS workout_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATETIME,
    end_date DATETIME,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_plans_member FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_workout_plans_member_status (member_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workout_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day_of_week VARCHAR(50),
    focus_area VARCHAR(255),
    CONSTRAINT fk_workout_sessions_plan FOREIGN KEY (plan_id) REFERENCES workout_plans(id),
    INDEX idx_workout_sessions_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workout_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    exercise_name VARCHAR(255) NOT NULL,
    sets INT,
    reps VARCHAR(50),
    notes TEXT,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_customized BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_workout_details_session FOREIGN KEY (session_id) REFERENCES workout_sessions(id),
    INDEX idx_workout_details_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workout_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    calories_burned DOUBLE,
    duration_minutes INT,
    workout_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_logs_member FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_workout_logs_member_date (member_id, workout_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- nutrition ----------
ALTER TABLE nutrition_plans
    ADD COLUMN IF NOT EXISTS ai_plan_id BIGINT NULL AFTER member_id;

UPDATE nutrition_plans
SET ai_plan_id = ai_workout_plan_id
WHERE ai_plan_id IS NULL AND ai_workout_plan_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS meal_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutrition_plan_id BIGINT NOT NULL,
    meal_name VARCHAR(100) NOT NULL,
    food_items TEXT NOT NULL,
    calories DOUBLE,
    is_customized BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_meal_details_plan FOREIGN KEY (nutrition_plan_id) REFERENCES nutrition_plans(id),
    INDEX idx_meal_details_plan (nutrition_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- ai_chat_histories ----------
CREATE TABLE IF NOT EXISTS ai_chat_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    sender_role VARCHAR(20) NOT NULL,
    message_content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_chat_histories_member FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_ai_chat_histories_member_created (member_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



