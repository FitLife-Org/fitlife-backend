package com.fitlife.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiPromptVersion {
    FULL_PLAN_V1(AiSuggestionType.FULL_PLAN, "FULL_PLAN_V1"),
    BODY_ANALYSIS_V1(AiSuggestionType.BODY_ANALYSIS, "BODY_ANALYSIS_V1"),
    WORKOUT_PLAN_V1(AiSuggestionType.WORKOUT_PLAN, "WORKOUT_PLAN_V1"),
    NUTRITION_PLAN_V1(AiSuggestionType.NUTRITION_PLAN, "NUTRITION_PLAN_V1");

    private final AiSuggestionType suggestionType;
    private final String code;
}