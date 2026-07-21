package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;

/**
 * Kiểm tra dữ liệu AI sau khi parse JSON và trước khi lưu DB.
 */
public interface AiResponseValidatorService {

    void validateFullPlan(
            AiGeneratedPlanResponse response,
            AiInputSnapshot snapshot
    );

    void validateBodyAnalysis(
            AiGeneratedBodyAnalysisResponse response,
            AiInputSnapshot snapshot
    );

    void validateWorkoutPlan(
            AiGeneratedWorkoutPlanResponse response,
            AiInputSnapshot snapshot
    );

    void validateNutritionPlan(
            AiGeneratedNutritionPlanResponse response,
            AiInputSnapshot snapshot
    );
}