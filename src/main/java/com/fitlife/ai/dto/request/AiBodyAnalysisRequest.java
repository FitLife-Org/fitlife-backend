package com.fitlife.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiBodyAnalysisRequest {

    private Long bodyMetricId;

    @NotBlank(message = "AI_GOAL_REQUIRED")
    private String goal;

    private String userNote;
}