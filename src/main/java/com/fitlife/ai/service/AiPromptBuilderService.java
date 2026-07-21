package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;

public interface AiPromptBuilderService {
    AiPromptResult buildFullPlanPrompt(AiInputSnapshot snapshot);
    AiPromptResult buildBodyAnalysisPrompt(AiInputSnapshot snapshot);

    AiPromptResult buildWorkoutPlanPrompt(
            AiInputSnapshot snapshot
    );

    AiPromptResult buildNutritionPlanPrompt(
            AiInputSnapshot snapshot
    );
}