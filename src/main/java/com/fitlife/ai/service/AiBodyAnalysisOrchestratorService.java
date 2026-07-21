package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;

public interface AiBodyAnalysisOrchestratorService {

    AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    );
}