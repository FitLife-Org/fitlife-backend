-- V19__create_nutrition_plan_module.sql
-- FitLife - Nutrition Plan domain

CREATE TABLE nutrition_plans
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id               BIGINT       NOT NULL,
    ai_suggestion_id        BIGINT       NULL,

    name                    VARCHAR(150) NOT NULL,
    description             TEXT         NULL,
    goal                    VARCHAR(100) NOT NULL,

    source                  VARCHAR(30)  NOT NULL,
    status                  VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',

    duration_weeks          INT          NOT NULL DEFAULT 4,

    daily_calories          INT           NULL,
    protein_grams           DECIMAL(8, 2) NULL,
    carbohydrate_grams      DECIMAL(8, 2) NULL,
    fat_grams               DECIMAL(8, 2) NULL,
    fiber_grams             DECIMAL(8, 2) NULL,

    meals_per_day           INT NULL,
    water_ml_per_day        INT NULL,

    start_date              DATE NULL,
    expected_end_date       DATE NULL,
    completed_at            DATETIME NULL,
    archived_at             DATETIME NULL,
    replacement_plan_id     BIGINT NULL,

    foods_to_limit          TEXT NULL,
    substitution_note       TEXT NULL,
    trainer_note            TEXT NULL,
    member_note             TEXT NULL,
    warning_message         TEXT NULL,

    modified_from_ai        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    created_by              BIGINT NULL,
    updated_by              BIGINT NULL,

    CONSTRAINT uk_nutrition_plans_ai_suggestion
        UNIQUE (ai_suggestion_id),

    CONSTRAINT fk_nutrition_plans_member
        FOREIGN KEY (member_id)
            REFERENCES members (id),

    CONSTRAINT fk_nutrition_plans_ai_suggestion
        FOREIGN KEY (ai_suggestion_id)
            REFERENCES ai_suggestions (id),

    CONSTRAINT fk_nutrition_plans_replacement
        FOREIGN KEY (replacement_plan_id)
            REFERENCES nutrition_plans (id),

    CONSTRAINT fk_nutrition_plans_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_nutrition_plans_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_nutrition_plans_duration_weeks
        CHECK (duration_weeks BETWEEN 1 AND 52),

    CONSTRAINT chk_nutrition_plans_daily_calories
        CHECK (
            daily_calories IS NULL
                OR daily_calories BETWEEN 500 AND 10000
            ),

    CONSTRAINT chk_nutrition_plans_meals_per_day
        CHECK (
            meals_per_day IS NULL
                OR meals_per_day BETWEEN 1 AND 10
            ),

    CONSTRAINT chk_nutrition_plans_water
        CHECK (
            water_ml_per_day IS NULL
                OR water_ml_per_day BETWEEN 0 AND 20000
            ),

    CONSTRAINT chk_nutrition_plans_date_range
        CHECK (
            start_date IS NULL
                OR expected_end_date IS NULL
                OR expected_end_date >= start_date
            )
);

CREATE TABLE nutrition_plan_items
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutrition_plan_id       BIGINT        NOT NULL,

    meal_name               VARCHAR(150)  NOT NULL,
    food_name               VARCHAR(200)  NOT NULL,

    quantity                DECIMAL(10, 2) NULL,
    unit                    VARCHAR(50)    NULL,
    portion_text            VARCHAR(255)  NULL,

    calories                INT           NULL,
    protein_grams           DECIMAL(8, 2) NULL,
    carbohydrate_grams      DECIMAL(8, 2) NULL,
    fat_grams               DECIMAL(8, 2) NULL,

    preparation             VARCHAR(500) NULL,
    substitution            VARCHAR(500) NULL,
    note                    TEXT NULL,

    sort_order              INT NOT NULL DEFAULT 0,

    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_nutrition_plan_items_plan
        FOREIGN KEY (nutrition_plan_id)
            REFERENCES nutrition_plans (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_nutrition_plan_items_calories
        CHECK (
            calories IS NULL
                OR calories >= 0
            ),

    CONSTRAINT chk_nutrition_plan_items_quantity
        CHECK (
            quantity IS NULL
                OR quantity >= 0
            )
);

CREATE INDEX idx_nutrition_plans_member
    ON nutrition_plans (member_id);

CREATE INDEX idx_nutrition_plans_member_status
    ON nutrition_plans (
                        member_id,
                        status,
                        is_deleted
        );

CREATE INDEX idx_nutrition_plans_source
    ON nutrition_plans (source);

CREATE INDEX idx_nutrition_plan_items_plan
    ON nutrition_plan_items (nutrition_plan_id);

CREATE INDEX idx_nutrition_plan_items_order
    ON nutrition_plan_items (
                             nutrition_plan_id,
                             sort_order
        );

ALTER TABLE ai_suggestions
    ADD CONSTRAINT fk_ai_suggestions_applied_nutrition_plan
        FOREIGN KEY (applied_nutrition_plan_id)
            REFERENCES nutrition_plans (id);