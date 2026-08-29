package com.fitlife.workout.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanCreateRequest {

    @NotBlank(
            message = "WORKOUT_PLAN_NAME_REQUIRED"
    )
    @Size(
            max = 150,
            message = "WORKOUT_PLAN_NAME_TOO_LONG"
    )
    private String name;

    @NotBlank(
            message = "WORKOUT_PLAN_GOAL_REQUIRED"
    )
    @Size(
            max = 100,
            message = "WORKOUT_PLAN_GOAL_TOO_LONG"
    )
    private String goal;

    @Size(
            max = 50,
            message = "WORKOUT_EXPERIENCE_LEVEL_TOO_LONG"
    )
    private String experienceLevel;

    @Min(
            value = 1,
            message = "WORKOUT_DURATION_WEEKS_MIN"
    )
    @Max(
            value = 52,
            message = "WORKOUT_DURATION_WEEKS_MAX"
    )
    private Integer durationWeeks;

    @Min(
            value = 1,
            message = "WORKOUT_DAYS_PER_WEEK_MIN"
    )
    @Max(
            value = 7,
            message = "WORKOUT_DAYS_PER_WEEK_MAX"
    )
    private Integer workoutDaysPerWeek;

    @Min(
            value = 10,
            message = "WORKOUT_DURATION_MINUTES_MIN"
    )
    @Max(
            value = 600,
            message = "WORKOUT_DURATION_MINUTES_MAX"
    )
    private Integer workoutDurationMinutes;

    @Size(
            max = 5000,
            message = "WORKOUT_DESCRIPTION_TOO_LONG"
    )
    private String description;

    @Size(
            max = 5000,
            message = "WORKOUT_NOTE_TOO_LONG"
    )
    private String note;

    @Valid
    @Builder.Default
    private List<WorkoutPlanDayRequest> days =
            new ArrayList<>();
}