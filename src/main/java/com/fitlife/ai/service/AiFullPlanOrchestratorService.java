package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

/**
 * Điều phối toàn bộ use case tạo AI Full Plan.
 */
public interface AiFullPlanOrchestratorService {

    AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    );
}