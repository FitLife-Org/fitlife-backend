package com.fitlife.ai.dto.request;

import com.fitlife.member.enums.FitnessGoal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiBodyAnalysisRequest {

    private Long bodyMetricId;

    @NotNull(message = "AI_GOAL_REQUIRED")
    private FitnessGoal goal;

    @Size(max = 1000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;
}