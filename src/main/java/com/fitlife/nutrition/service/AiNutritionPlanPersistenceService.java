package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.response.NutritionPlanResponse;

public interface AiNutritionPlanPersistenceService {
    NutritionPlanResponse persistAiSuggestion(Long suggestionId, Long memberId);
}
