-- V15__create_workout_plan_module.sql
-- FitLife - New Workout Plan domain
-- Creates the new business-domain workout tables.
-- Legacy tables are removed in V16 after this migration succeeds.

CREATE TABLE workout_plans
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id                BIGINT       NOT NULL,
    trainer_id               BIGINT       NULL,
    source_ai_suggestion_id  BIGINT       NULL,

    code                     VARCHAR(50)  NOT NULL,
    name                     VARCHAR(150) NOT NULL,
    goal                     VARCHAR(100) NOT NULL,
    experience_level         VARCHAR(50)  NULL,

    duration_weeks           INT          NOT NULL DEFAULT 4,
    workout_days_per_week    INT          NOT NULL DEFAULT 3,
    workout_duration_minutes INT          NULL,

    start_date               DATE         NULL,
    end_date                 DATE         NULL,

    description              TEXT         NULL,
    note                     TEXT         NULL,

    source_type              VARCHAR(30)  NOT NULL DEFAULT 'MANUAL',
    status                   VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',

    created_by               BIGINT       NULL,
    updated_by               BIGINT       NULL,
    is_deleted               TINYINT(1)   NOT NULL DEFAULT 0,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_workout_plans_code
        UNIQUE (code),

    CONSTRAINT fk_workout_plans_member
        FOREIGN KEY (member_id) REFERENCES members (id),

    CONSTRAINT fk_workout_plans_trainer
        FOREIGN KEY (trainer_id) REFERENCES trainers (id),

    CONSTRAINT fk_workout_plans_ai_suggestion
        FOREIGN KEY (source_ai_suggestion_id) REFERENCES ai_suggestions (id),

    CONSTRAINT fk_workout_plans_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_workout_plans_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id),

    CONSTRAINT chk_workout_plans_duration_weeks
        CHECK (duration_weeks BETWEEN 1 AND 52),

    CONSTRAINT chk_workout_plans_days_per_week
        CHECK (workout_days_per_week BETWEEN 1 AND 7),

    CONSTRAINT chk_workout_plans_duration_minutes
        CHECK (
            workout_duration_minutes IS NULL
                OR workout_duration_minutes BETWEEN 10 AND 300
            ),

    CONSTRAINT chk_workout_plans_date_range
        CHECK (
            start_date IS NULL
                OR end_date IS NULL
                OR end_date >= start_date
            )
);

CREATE INDEX idx_workout_plans_member
    ON workout_plans (member_id);

CREATE INDEX idx_workout_plans_trainer
    ON workout_plans (trainer_id);

CREATE INDEX idx_workout_plans_status
    ON workout_plans (status);

CREATE INDEX idx_workout_plans_source_type
    ON workout_plans (source_type);

CREATE INDEX idx_workout_plans_member_status
    ON workout_plans (member_id, status, is_deleted);

CREATE INDEX idx_workout_plans_ai_suggestion
    ON workout_plans (source_ai_suggestion_id);

CREATE INDEX idx_workout_plans_dates
    ON workout_plans (start_date, end_date);


CREATE TABLE workout_plan_days
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_plan_id   BIGINT       NOT NULL,

    week_no           INT          NOT NULL DEFAULT 1,
    day_no            INT          NOT NULL,
    day_of_week       VARCHAR(20)  NULL,

    name              VARCHAR(150) NOT NULL,
    focus_area        VARCHAR(150) NULL,
    estimated_minutes INT          NULL,
    note              TEXT         NULL,

    sort_order        INT          NOT NULL DEFAULT 0,
    is_rest_day       TINYINT(1)   NOT NULL DEFAULT 0,

    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_plan_days_plan
        FOREIGN KEY (workout_plan_id)
            REFERENCES workout_plans (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_workout_plan_days_week_day
        UNIQUE (workout_plan_id, week_no, day_no),

    CONSTRAINT chk_workout_plan_days_week_no
        CHECK (week_no BETWEEN 1 AND 52),

    CONSTRAINT chk_workout_plan_days_day_no
        CHECK (day_no BETWEEN 1 AND 7),

    CONSTRAINT chk_workout_plan_days_estimated_minutes
        CHECK (
            estimated_minutes IS NULL
                OR estimated_minutes BETWEEN 0 AND 300
            )
);

CREATE INDEX idx_workout_plan_days_plan
    ON workout_plan_days (workout_plan_id);

CREATE INDEX idx_workout_plan_days_order
    ON workout_plan_days (workout_plan_id, week_no, sort_order);


CREATE TABLE workout_exercises
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_plan_day_id BIGINT        NOT NULL,

    exercise_name       VARCHAR(150)  NOT NULL,
    target_muscle       VARCHAR(100)  NULL,
    equipment_id        BIGINT        NULL,

    sets                INT           NULL,
    reps                VARCHAR(50)   NULL,
    weight_kg           DECIMAL(7, 2) NULL,
    duration_minutes    INT           NULL,
    distance_km         DECIMAL(7, 2) NULL,
    rest_seconds        INT           NULL,

    tempo               VARCHAR(30)   NULL,
    rpe                  DECIMAL(3, 1) NULL,

    instruction         TEXT          NULL,
    note                TEXT          NULL,
    video_url           VARCHAR(500)  NULL,

    sort_order          INT           NOT NULL DEFAULT 0,
    is_optional         TINYINT(1)    NOT NULL DEFAULT 0,

    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_exercises_day
        FOREIGN KEY (workout_plan_day_id)
            REFERENCES workout_plan_days (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workout_exercises_equipment
        FOREIGN KEY (equipment_id)
            REFERENCES equipment (id),

    CONSTRAINT chk_workout_exercises_sets
        CHECK (sets IS NULL OR sets BETWEEN 1 AND 100),

    CONSTRAINT chk_workout_exercises_weight
        CHECK (weight_kg IS NULL OR weight_kg >= 0),

    CONSTRAINT chk_workout_exercises_duration
        CHECK (
            duration_minutes IS NULL
                OR duration_minutes BETWEEN 0 AND 600
            ),

    CONSTRAINT chk_workout_exercises_distance
        CHECK (distance_km IS NULL OR distance_km >= 0),

    CONSTRAINT chk_workout_exercises_rest_seconds
        CHECK (
            rest_seconds IS NULL
                OR rest_seconds BETWEEN 0 AND 3600
            ),

    CONSTRAINT chk_workout_exercises_rpe
        CHECK (
            rpe IS NULL
                OR rpe BETWEEN 1.0 AND 10.0
            )
);

CREATE INDEX idx_workout_exercises_day
    ON workout_exercises (workout_plan_day_id);

CREATE INDEX idx_workout_exercises_equipment
    ON workout_exercises (equipment_id);

CREATE INDEX idx_workout_exercises_name
    ON workout_exercises (exercise_name);

CREATE INDEX idx_workout_exercises_order
    ON workout_exercises (workout_plan_day_id, sort_order);


CREATE TABLE workout_plan_assignments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_plan_id BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    assigned_by     BIGINT      NULL,

    assigned_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date      DATE        NULL,
    end_date        DATE        NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    note            TEXT        NULL,

    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_plan_assignments_plan
        FOREIGN KEY (workout_plan_id)
            REFERENCES workout_plans (id),

    CONSTRAINT fk_workout_plan_assignments_member
        FOREIGN KEY (member_id)
            REFERENCES members (id),

    CONSTRAINT fk_workout_plan_assignments_assigned_by
        FOREIGN KEY (assigned_by)
            REFERENCES users (id),

    CONSTRAINT chk_workout_plan_assignments_date_range
        CHECK (
            start_date IS NULL
                OR end_date IS NULL
                OR end_date >= start_date
            )
);

CREATE INDEX idx_workout_plan_assignments_plan
    ON workout_plan_assignments (workout_plan_id);

CREATE INDEX idx_workout_plan_assignments_member
    ON workout_plan_assignments (member_id);

CREATE INDEX idx_workout_plan_assignments_member_status
    ON workout_plan_assignments (member_id, status);


-- ai_suggestions.applied_workout_plan_id already exists.
-- Add the missing FK to the new workout domain.
ALTER TABLE ai_suggestions
    ADD CONSTRAINT fk_ai_suggestions_applied_workout_plan
        FOREIGN KEY (applied_workout_plan_id)
            REFERENCES workout_plans (id);

-- Business values:
-- workout_plans.source_type: MANUAL, AI, TEMPLATE
-- workout_plans.status: DRAFT, ACTIVE, COMPLETED, CANCELLED, ARCHIVED
-- workout_plan_assignments.status: ACTIVE, COMPLETED, CANCELLED
