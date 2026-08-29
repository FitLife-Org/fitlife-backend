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
public class WorkoutPlanDayRequest {

    @Min(
            value = 1,
            message = "WORKOUT_WEEK_NO_MIN"
    )
    @Max(
            value = 52,
            message = "WORKOUT_WEEK_NO_MAX"
    )
    private Integer weekNo;

    @Min(
            value = 1,
            message = "WORKOUT_DAY_NO_MIN"
    )
    @Max(
            value = 7,
            message = "WORKOUT_DAY_NO_MAX"
    )
    private Integer dayNo;

    @Size(
            max = 20,
            message = "WORKOUT_DAY_OF_WEEK_TOO_LONG"
    )
    private String dayOfWeek;

    @NotBlank(
            message = "WORKOUT_DAY_NAME_REQUIRED"
    )
    @Size(
            max = 150,
            message = "WORKOUT_DAY_NAME_TOO_LONG"
    )
    private String name;

    @Size(
            max = 150,
            message = "WORKOUT_FOCUS_AREA_TOO_LONG"
    )
    private String focusArea;

    @Min(
            value = 0,
            message = "WORKOUT_ESTIMATED_MINUTES_MIN"
    )
    @Max(
            value = 600,
            message = "WORKOUT_ESTIMATED_MINUTES_MAX"
    )
    private Integer estimatedMinutes;

    @Size(
            max = 5000,
            message = "WORKOUT_DAY_NOTE_TOO_LONG"
    )
    private String note;

    @Min(
            value = 0,
            message = "WORKOUT_SORT_ORDER_MIN"
    )
    private Integer sortOrder;

    @Builder.Default
    private Boolean isRestDay = false;

    @Valid
    @Builder.Default
    private List<WorkoutExerciseRequest> exercises =
            new ArrayList<>();
}