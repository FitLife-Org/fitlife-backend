package com.fitlife.ai.dto.request;

import com.fitlife.ai.enums.ActivityLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiNutritionPlanRequest {

    @NotBlank(message = "AI_GOAL_REQUIRED")
    private String goal;

    private ActivityLevel activityLevel;

    @Min(value = 1, message = "MEALS_PER_DAY_INVALID")
    @Max(value = 8, message = "MEALS_PER_DAY_INVALID")
    private Integer mealsPerDay;

    private String userNote;
}