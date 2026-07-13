-- ============================================================
-- V11__update_trainers_table.sql
-- Purpose:
--   Standardize the trainers table created in V1.
--
-- Changes:
--   1. Remove duplicated user information from trainers.
--   2. Require every trainer to be linked to a user.
--   3. Add certifications.
--   4. Rename is_deleted to deleted.
--   5. Expand specialization length.
--   6. Add indexes for trainer filtering.
-- ============================================================

-- ------------------------------------------------------------
-- PRECONDITION
-- Every trainer must be linked to an existing user before
-- changing user_id to NOT NULL.
-- ------------------------------------------------------------

-- Remove duplicated information already stored in users.
ALTER TABLE trainers
DROP COLUMN full_name,
    DROP COLUMN phone,
    DROP COLUMN email;

-- Standardize trainer business fields.
ALTER TABLE trainers
    MODIFY COLUMN user_id BIGINT NOT NULL,
    MODIFY COLUMN trainer_code VARCHAR(50) NOT NULL,
    MODIFY COLUMN specialization VARCHAR(255) NULL,
    MODIFY COLUMN experience_years INT NULL DEFAULT 0,
    ADD COLUMN certifications TEXT NULL AFTER experience_years,
    MODIFY COLUMN bio TEXT NULL,
    MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    CHANGE COLUMN is_deleted deleted BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at DATETIME
    NOT NULL
    DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP;

-- Additional indexes used by trainer search and filtering.
CREATE INDEX idx_trainers_deleted
    ON trainers (deleted);

CREATE INDEX idx_trainers_specialization
    ON trainers (specialization);