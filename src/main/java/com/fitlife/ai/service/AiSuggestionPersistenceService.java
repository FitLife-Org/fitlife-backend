package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;

/**
 * Quản lý persistence và transaction lifecycle của AI Suggestion.
 *
 * Các method ghi dữ liệu sử dụng transaction độc lập để bảo đảm:
 * - PENDING vẫn tồn tại khi provider lỗi.
 * - FAILED vẫn được lưu khi orchestration ném exception.
 * - SUCCESS và plan items được lưu cùng một transaction.
 */
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
}