package com.fitlife.ai.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiApplyPlanResponse {

    private final Long suggestionId;

    private final Long workoutPlanId;

    private final Long nutritionPlanId;

    private final boolean workoutApplied;

    private final boolean nutritionApplied;

    private final String message;
}