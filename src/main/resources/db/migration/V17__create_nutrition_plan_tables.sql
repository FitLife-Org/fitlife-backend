CREATE TABLE nutrition_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    ai_suggestion_id BIGINT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    goal VARCHAR(50) NOT NULL,
    source VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    duration_weeks INT NOT NULL,
    daily_calories INT NULL,
    protein_grams DECIMAL(8,2) NULL,
    carbohydrate_grams DECIMAL(8,2) NULL,
    fat_grams DECIMAL(8,2) NULL,
    fiber_grams DECIMAL(8,2) NULL,
    meals_per_day INT NULL,
    water_ml_per_day INT NULL,
    start_date DATE NULL,
    expected_end_date DATE NULL,
    completed_at DATETIME NULL,
    archived_at DATETIME NULL,
    replacement_plan_id BIGINT NULL,
    foods_to_limit TEXT NULL,
    substitution_note TEXT NULL,
    trainer_note TEXT NULL,
    member_note TEXT NULL,
    warning_message TEXT NULL,
    modified_from_ai BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_nutrition_plan_ai_suggestion UNIQUE (ai_suggestion_id),
    CONSTRAINT fk_nutrition_plan_member FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_nutrition_plan_ai_suggestion FOREIGN KEY (ai_suggestion_id) REFERENCES ai_suggestions(id),
    CONSTRAINT fk_nutrition_plan_replacement FOREIGN KEY (replacement_plan_id) REFERENCES nutrition_plans(id)
);

CREATE TABLE nutrition_plan_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutrition_plan_id BIGINT NOT NULL,
    meal_name VARCHAR(150) NOT NULL,
    food_name VARCHAR(200) NOT NULL,
    quantity DECIMAL(10,2) NULL,
    unit VARCHAR(50) NULL,
    portion_text VARCHAR(150) NULL,
    calories INT NULL,
    protein_grams DECIMAL(8,2) NULL,
    carbohydrate_grams DECIMAL(8,2) NULL,
    fat_grams DECIMAL(8,2) NULL,
    preparation VARCHAR(500) NULL,
    substitution VARCHAR(500) NULL,
    note TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_plan_item_plan FOREIGN KEY (nutrition_plan_id) REFERENCES nutrition_plans(id)
);

CREATE INDEX idx_nutrition_plan_member ON nutrition_plans(member_id);
CREATE INDEX idx_nutrition_plan_member_status ON nutrition_plans(member_id, status, is_deleted);
CREATE INDEX idx_nutrition_plan_source ON nutrition_plans(source);
CREATE INDEX idx_nutrition_plan_item_plan ON nutrition_plan_items(nutrition_plan_id);



-- 2. Thêm mock nutrition plan
INSERT IGNORE INTO nutrition_plans (
    id, member_id, name, description, goal, source, status, duration_weeks, 
    daily_calories, protein_grams, carbohydrate_grams, fat_grams, fiber_grams,
    meals_per_day, water_ml_per_day, start_date, created_at, updated_at, modified_from_ai, is_deleted
) VALUES (
    1, 1, 'Kế hoạch Tăng Cơ Giảm Mỡ', 'Kế hoạch ăn kiêng khoa học trong 4 tuần', 'MUSCLE_GAIN', 'AI_GENERATED', 'ACTIVE', 4, 
    2200, 180.0, 200.0, 70.0, 30.0,
    3, 3000, CURDATE(), NOW(), NOW(), 0, 0
);

-- 3. Thêm mock nutrition plan items (2 món ăn cho bữa sáng)
INSERT IGNORE INTO nutrition_plan_items (
    id, nutrition_plan_id, meal_name, food_name, quantity, unit, portion_text,
    calories, protein_grams, carbohydrate_grams, fat_grams, created_at, updated_at
) VALUES 
(1, 1, 'Bữa Sáng', 'Ức gà luộc', 150, 'g', '1 miếng vừa', 248, 46.5, 0, 5.4, NOW(), NOW()),
(2, 1, 'Bữa Sáng', 'Khoai lang luộc', 200, 'g', '1 củ to', 172, 3.2, 40.2, 0.3, NOW(), NOW());
