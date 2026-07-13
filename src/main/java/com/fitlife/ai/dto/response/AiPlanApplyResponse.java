package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.AiSuggestionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiPlanApplyResponse {

    private Long aiSuggestionId;

    private Long workoutPlanId;

    private Long nutritionPlanId;

    private AiSuggestionStatus status;

    private String message;
}