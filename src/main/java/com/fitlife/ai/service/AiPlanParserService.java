package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;

public interface AiPlanParserService {

    AiGeneratedPlanResponse parseGeneratedPlan(
            String rawResponse
    );

    void savePlanItems(
            AiSuggestion aiSuggestion,
            AiGeneratedPlanResponse planResponse
    );

    AiGeneratedBodyAnalysisResponse parseBodyAnalysis(
            String rawResponse
    );

    void saveBodyAnalysisItems(
            AiSuggestion aiSuggestion,
            AiGeneratedBodyAnalysisResponse response
    );

    AiGeneratedWorkoutPlanResponse parseWorkoutPlan(
            String rawResponse
    );

    void saveWorkoutPlanItems(
            AiSuggestion aiSuggestion,
            AiGeneratedWorkoutPlanResponse response
    );

    AiGeneratedNutritionPlanResponse parseNutritionPlan(
            String rawResponse
    );

    void saveNutritionPlanItems(
            AiSuggestion suggestion,
            AiGeneratedNutritionPlanResponse response
    );
}