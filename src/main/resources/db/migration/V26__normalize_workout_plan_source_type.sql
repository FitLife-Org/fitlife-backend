-- ============================================================
-- V26 - Normalize Workout Plan Source Type
-- ============================================================
--
-- Standard values:
-- AI_GENERATED
-- TRAINER_CREATED
-- MEMBER_CREATED
-- MANUAL
--
-- Legacy:
-- AI      -> AI_GENERATED
-- TRAINER -> TRAINER_CREATED
-- CLONED  -> MEMBER_CREATED
-- ============================================================

UPDATE workout_plans
SET source_type = 'AI_GENERATED'
WHERE source_type = 'AI';

UPDATE workout_plans
SET source_type = 'TRAINER_CREATED'
WHERE source_type = 'TRAINER';

UPDATE workout_plans
SET source_type = 'MEMBER_CREATED'
WHERE source_type = 'CLONED';

UPDATE workout_plans
SET source_type = 'MANUAL'
WHERE source_type IS NULL
   OR TRIM(source_type) = '';