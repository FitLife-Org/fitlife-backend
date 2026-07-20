package com.fitlife.ai.dto.request;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.member.enums.FitnessGoal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiNutritionPlanRequest {

    @NotNull(message = "AI_GOAL_REQUIRED")
    private FitnessGoal goal;

    @NotNull(message = "AI_ACTIVITY_LEVEL_REQUIRED")
    private ActivityLevel activityLevel;

    @NotNull(message = "MEALS_PER_DAY_REQUIRED")
    @Min(value = 2, message = "MEALS_PER_DAY_INVALID")
    @Max(value = 6, message = "MEALS_PER_DAY_INVALID")
    private Integer mealsPerDay;

    @Size(max = 2000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;

    @Pattern(
            regexp = "^(vi|en)$",
            message = "AI_PREFERRED_LANGUAGE_INVALID"
    )
    private String preferredLanguage = "vi";
}