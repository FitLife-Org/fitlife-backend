package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

public interface AiWorkoutPlanOrchestratorService {

    AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    );
}