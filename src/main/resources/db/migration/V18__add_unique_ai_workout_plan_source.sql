-- V18__add_unique_ai_workout_plan_source.sql
-- FitLife - AI suggestion apply protection

CREATE UNIQUE INDEX uk_workout_plans_ai_suggestion
    ON workout_plans (source_ai_suggestion_id);