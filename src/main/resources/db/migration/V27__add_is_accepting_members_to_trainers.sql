-- ============================================================
-- V27__add_is_accepting_members_to_trainers.sql
-- Purpose:
--   Add is_accepting_members column to trainers table.
-- ============================================================

ALTER TABLE trainers
    ADD COLUMN is_accepting_members BOOLEAN NOT NULL DEFAULT TRUE;
