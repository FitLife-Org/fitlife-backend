package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiApplyPlanResponse;

public interface AiSuggestionApplyService {

    AiApplyPlanResponse applyWorkoutPlan(
            Long suggestionId
    );
}