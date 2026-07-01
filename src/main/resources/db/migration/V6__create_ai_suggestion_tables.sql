-- FitLife V6
-- Create AI suggestion tables
-- Module: AI Personalized Recommendation
-- Note:
-- 1. AI uses users + members + body_metrics as input.
-- 2. members no longer stores full_name, phone, email, avatar_url, height_cm, weight_kg, bmi.
-- 3. Profile data should be read from users.
-- 4. Body data should be read from body_metrics.

CREATE TABLE ai_suggestions
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,

    member_id                   BIGINT       NOT NULL,
    latest_body_metric_id        BIGINT,

    suggestion_type              VARCHAR(50)  NOT NULL,
    goal                         VARCHAR(100) NOT NULL,
    experience_level             VARCHAR(50),
    activity_level               VARCHAR(50),

    workout_days_per_week        INT,
    workout_duration_minutes     INT,

    user_note                    TEXT,

    input_snapshot               JSON         NOT NULL,
    ai_response                  JSON,

    summary                      TEXT,
    warning_message              TEXT,

    status                       VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    error_message                TEXT,

    applied_workout_plan_id      BIGINT,
    applied_nutrition_plan_id    BIGINT,

    created_at                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    created_by                   BIGINT,
    updated_by                   BIGINT,

    is_deleted                   BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_ai_suggestions_member
        FOREIGN KEY (member_id) REFERENCES members (id),

    CONSTRAINT fk_ai_suggestions_body_metric
        FOREIGN KEY (latest_body_metric_id) REFERENCES body_metrics (id),

    CONSTRAINT fk_ai_suggestions_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_ai_suggestions_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id),

    INDEX idx_ai_suggestions_member (member_id),
    INDEX idx_ai_suggestions_type (suggestion_type),
    INDEX idx_ai_suggestions_status (status),
    INDEX idx_ai_suggestions_goal (goal),
    INDEX idx_ai_suggestions_created_at (created_at),
    INDEX idx_ai_suggestions_member_status (member_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE ai_plan_items
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    ai_suggestion_id     BIGINT       NOT NULL,

    item_type            VARCHAR(50)  NOT NULL,

    title                VARCHAR(255) NOT NULL,
    description          TEXT,

    day_no               INT,
    day_of_week          VARCHAR(30),

    exercise_name        VARCHAR(150),
    sets                 INT,
    reps                 VARCHAR(50),
    duration_minutes     INT,

    meal_name            VARCHAR(100),
    calories             INT,
    protein_grams        DECIMAL(7, 2),
    carbs_grams          DECIMAL(7, 2),
    fat_grams            DECIMAL(7, 2),

    sort_order           INT          NOT NULL DEFAULT 0,

    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_plan_items_suggestion
        FOREIGN KEY (ai_suggestion_id) REFERENCES ai_suggestions (id),

    INDEX idx_ai_plan_items_suggestion (ai_suggestion_id),
    INDEX idx_ai_plan_items_type (item_type),
    INDEX idx_ai_plan_items_sort (ai_suggestion_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE ai_feedbacks
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    ai_suggestion_id     BIGINT      NOT NULL,
    member_id            BIGINT      NOT NULL,

    rating               INT         NOT NULL,
    comment              TEXT,

    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_feedbacks_suggestion
        FOREIGN KEY (ai_suggestion_id) REFERENCES ai_suggestions (id),

    CONSTRAINT fk_ai_feedbacks_member
        FOREIGN KEY (member_id) REFERENCES members (id),

    CONSTRAINT uk_ai_feedbacks_suggestion_member
        UNIQUE (ai_suggestion_id, member_id),

    INDEX idx_ai_feedbacks_member (member_id),
    INDEX idx_ai_feedbacks_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;