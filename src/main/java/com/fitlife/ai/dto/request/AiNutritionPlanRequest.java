package com.fitlife.ai.dto.request;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.member.enums.FitnessGoal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiNutritionPlanRequest {

    @NotNull(message = "AI_GOAL_REQUIRED")
    private FitnessGoal goal;

    private ActivityLevel activityLevel;

    @Min(value = 1, message = "MEALS_PER_DAY_INVALID")
    @Max(value = 8, message = "MEALS_PER_DAY_INVALID")
    private Integer mealsPerDay;

    @Size(max = 1000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;
}