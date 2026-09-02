package com.fitlife.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiFeedbackRequest {

    @NotNull(message = "AI_FEEDBACK_RATING_REQUIRED")
    @Min(value = 1, message = "AI_FEEDBACK_RATING_INVALID")
    @Max(value = 5, message = "AI_FEEDBACK_RATING_INVALID")
    private Integer rating;

    private Boolean useful;

    @Size(max = 2000, message = "AI_FEEDBACK_COMMENT_TOO_LONG")
    private String comment;
}