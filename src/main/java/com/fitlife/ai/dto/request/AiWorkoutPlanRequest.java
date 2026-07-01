package com.fitlife.ai.dto.request;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiWorkoutPlanRequest {

    @NotBlank(message = "AI_GOAL_REQUIRED")
    private String goal;

    private ExperienceLevel experienceLevel;

    private ActivityLevel activityLevel;

    @Min(value = 1, message = "WORKOUT_DAYS_INVALID")
    @Max(value = 7, message = "WORKOUT_DAYS_INVALID")
    private Integer workoutDaysPerWeek;

    @Min(value = 15, message = "WORKOUT_DURATION_INVALID")
    @Max(value = 240, message = "WORKOUT_DURATION_INVALID")
    private Integer workoutDurationMinutes;

    private String userNote;
}