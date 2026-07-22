package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;

public interface AiPromptBuilderService {

    AiPromptResult buildFullPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    );

    AiPromptResult buildBodyAnalysisPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    );

    AiPromptResult buildWorkoutPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    );

    AiPromptResult buildNutritionPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    );
}