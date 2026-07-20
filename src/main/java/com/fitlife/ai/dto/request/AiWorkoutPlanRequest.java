package com.fitlife.ai.dto.request;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
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
public class AiWorkoutPlanRequest {

    @NotNull(message = "AI_GOAL_REQUIRED")
    private FitnessGoal goal;

    @NotNull(message = "AI_EXPERIENCE_LEVEL_REQUIRED")
    private ExperienceLevel experienceLevel;

    @NotNull(message = "AI_ACTIVITY_LEVEL_REQUIRED")
    private ActivityLevel activityLevel;

    @NotNull(message = "WORKOUT_DAYS_REQUIRED")
    @Min(value = 2, message = "WORKOUT_DAYS_INVALID")
    @Max(value = 6, message = "WORKOUT_DAYS_INVALID")
    private Integer workoutDaysPerWeek;

    @NotNull(message = "WORKOUT_DURATION_REQUIRED")
    @Min(value = 20, message = "WORKOUT_DURATION_INVALID")
    @Max(value = 180, message = "WORKOUT_DURATION_INVALID")
    private Integer workoutDurationMinutes;

    @Size(max = 2000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;

    @Pattern(
            regexp = "^(vi|en)$",
            message = "AI_PREFERRED_LANGUAGE_INVALID"
    )
    private String preferredLanguage = "vi";
}