package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.*;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AiSuggestionService {

    AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    );

    AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    );

    AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    );

    PageResponse<AiSuggestionResponse> getMySuggestions(
            Pageable pageable
    );

    PageResponse<AiSuggestionResponse> getMySuggestionsByFilter(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    AiSuggestionDetailResponse getMySuggestionDetail(
            Long id
    );

    AiFeedbackResponse submitFeedback(
            Long aiSuggestionId,
            AiFeedbackRequest request
    );

    AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    );
}