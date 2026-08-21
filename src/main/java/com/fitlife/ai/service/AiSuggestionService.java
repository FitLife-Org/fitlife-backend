package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;

import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;

import com.fitlife.common.response.PageResponse;

import org.springframework.data.domain.Pageable;

public interface AiSuggestionService {

    // =====================================================
    // GENERATE
    // =====================================================

    AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    );

    AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    );

    AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    );

    AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    );

    // =====================================================
    // MEMBER
    // =====================================================

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

    // =====================================================
    // ADMIN
    // =====================================================

    /**
     * Admin xem toàn bộ AI Suggestion.
     *
     * suggestionType = null -> không filter type.
     * status = null         -> không filter status.
     */
    PageResponse<AiSuggestionResponse> getAdminSuggestions(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    /**
     * Admin xem chi tiết AI Suggestion
     * mà không áp dụng ownership của Member.
     */
    AiSuggestionDetailResponse getAdminSuggestionDetail(
            Long id
    );
}