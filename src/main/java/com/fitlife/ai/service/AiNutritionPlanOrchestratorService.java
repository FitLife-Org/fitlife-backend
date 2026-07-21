package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

public interface AiNutritionPlanOrchestratorService {

    AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    );
}