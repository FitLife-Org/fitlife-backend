package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;

public interface AiSuggestionPersistenceService {

    AiSuggestion createPending(
            AiSuggestion suggestion
    );

    AiSuggestion markFullPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedPlanResponse generatedPlan,
            String warningMessage
    );

    AiSuggestion markWorkoutPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedWorkoutPlanResponse generated,
            String warningMessage
    );

    AiSuggestion markNutritionPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedNutritionPlanResponse generated,
            String warningMessage
    );

    AiSuggestion markBodyAnalysisSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedBodyAnalysisResponse analysis,
            String warningMessage
    );

    AiSuggestion markFailed(
            Long suggestionId,
            String errorCode,
            String errorMessage
    );
}