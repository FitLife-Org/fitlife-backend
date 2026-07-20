-- V16__remove_legacy_checkin_workout_nutrition_tables.sql
-- FitLife cleanup migration.
--
-- WARNING:
-- This migration permanently removes data from the listed legacy tables.
-- Back up the database before running it.
--
-- Removed:
-- 1. checkins                          (old check-in table)
-- 2. ai_workout_plan_items            (old AI workout item table)
-- 3. ai_workout_plans                 (old AI workout plan table)
-- 4. nutrition_plan_items             (old nutrition item table)
-- 5. nutrition_plans                  (old nutrition plan table)
--
-- Kept:
-- check_ins                           (current check-in table)
-- workout_plans                       (new V15 workout table)
-- workout_plan_days
-- workout_exercises
-- workout_plan_assignments
-- ai_suggestions
-- ai_plan_items
--
-- ai_suggestions.applied_nutrition_plan_id is intentionally kept
-- as a nullable placeholder for the rebuilt Nutrition module.

-- =========================================================
-- 1. REMOVE OLD CHECK-IN TABLE
-- =========================================================

DROP TABLE checkins;


-- =========================================================
-- 2. REMOVE OLD NUTRITION TABLES
-- =========================================================
-- nutrition_plans references ai_workout_plans, therefore
-- Nutrition must be dropped before ai_workout_plans.

DROP TABLE nutrition_plan_items;
DROP TABLE nutrition_plans;


-- =========================================================
-- 3. REMOVE OLD AI WORKOUT TABLES
-- =========================================================

DROP TABLE ai_workout_plan_items;
DROP TABLE ai_workout_plans;
