-- =========================================================
-- FitLife V14
-- Upgrade AI Personalized Recommendation schema
--
-- Important:
-- 1. Do not modify V6 because it has already been applied.
-- 2. Current Flyway version before this migration is V13.
-- 3. This migration adds AI processing metadata,
--    Qdrant context snapshot and additional plan fields.
-- =========================================================


-- =========================================================
-- 1. Upgrade ai_suggestions
-- =========================================================

ALTER TABLE ai_suggestions
    ADD COLUMN preferred_language VARCHAR(10)
        NOT NULL DEFAULT 'vi'
    AFTER user_note,

    ADD COLUMN context_snapshot JSON
        NULL
        AFTER input_snapshot,

    ADD COLUMN provider VARCHAR(30)
        NOT NULL DEFAULT 'GEMINI'
        AFTER warning_message,

    ADD COLUMN model_name VARCHAR(100)
        NULL
        AFTER provider,

    ADD COLUMN prompt_version VARCHAR(50)
        NULL
        AFTER model_name,

    ADD COLUMN provider_request_id VARCHAR(255)
        NULL
        AFTER prompt_version,

    ADD COLUMN error_code VARCHAR(100)
        NULL
        AFTER status,

    ADD COLUMN requested_at DATETIME
        NULL
        AFTER error_message,

    ADD COLUMN completed_at DATETIME
        NULL
        AFTER requested_at;


-- Backfill requested_at for existing AI suggestions.
UPDATE ai_suggestions
SET requested_at = created_at
WHERE requested_at IS NULL;


-- After backfill, requested_at becomes required.
ALTER TABLE ai_suggestions
    MODIFY COLUMN requested_at DATETIME NOT NULL;


-- =========================================================
-- 2. Upgrade ai_plan_items
-- =========================================================

ALTER TABLE ai_plan_items
    ADD COLUMN rest_seconds INT
    NULL
        AFTER reps,

    ADD COLUMN portion_text VARCHAR(255)
        NULL
        AFTER meal_name;


-- =========================================================
-- 3. Upgrade ai_feedbacks
-- =========================================================

ALTER TABLE ai_feedbacks
    ADD COLUMN useful BOOLEAN
    NULL
        AFTER rating;


-- Rating must be between 1 and 5.
ALTER TABLE ai_feedbacks
    ADD CONSTRAINT chk_ai_feedbacks_rating
        CHECK (rating BETWEEN 1 AND 5);


-- =========================================================
-- 4. Additional indexes
-- =========================================================

CREATE INDEX idx_ai_suggestions_member_created
    ON ai_suggestions(member_id, created_at);

CREATE INDEX idx_ai_suggestions_daily_usage
    ON ai_suggestions(
                      member_id,
                      requested_at,
                      status,
                      is_deleted
        );

CREATE INDEX idx_ai_suggestions_provider
    ON ai_suggestions(provider);

CREATE INDEX idx_ai_suggestions_prompt_version
    ON ai_suggestions(prompt_version);

CREATE INDEX idx_ai_suggestions_completed_at
    ON ai_suggestions(completed_at);