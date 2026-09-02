package com.fitlife.workout.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExerciseRequest {

    @NotBlank(
            message = "WORKOUT_EXERCISE_NAME_REQUIRED"
    )
    @Size(
            max = 150,
            message = "WORKOUT_EXERCISE_NAME_TOO_LONG"
    )
    private String exerciseName;

    @Size(
            max = 100,
            message = "WORKOUT_TARGET_MUSCLE_TOO_LONG"
    )
    private String targetMuscle;

    @PositiveOrZero(
            message = "WORKOUT_EQUIPMENT_ID_INVALID"
    )
    private Long equipmentId;

    @Min(
            value = 1,
            message = "WORKOUT_SETS_MIN"
    )
    @Max(
            value = 100,
            message = "WORKOUT_SETS_MAX"
    )
    private Integer sets;

    @Size(
            max = 50,
            message = "WORKOUT_REPS_TOO_LONG"
    )
    private String reps;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "WORKOUT_WEIGHT_INVALID"
    )
    private BigDecimal weightKg;

    @Min(
            value = 0,
            message = "WORKOUT_EXERCISE_DURATION_MIN"
    )
    @Max(
            value = 600,
            message = "WORKOUT_EXERCISE_DURATION_MAX"
    )
    private Integer durationMinutes;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "WORKOUT_DISTANCE_INVALID"
    )
    private BigDecimal distanceKm;

    @Min(
            value = 0,
            message = "WORKOUT_REST_SECONDS_MIN"
    )
    @Max(
            value = 3600,
            message = "WORKOUT_REST_SECONDS_MAX"
    )
    private Integer restSeconds;

    @Size(
            max = 30,
            message = "WORKOUT_TEMPO_TOO_LONG"
    )
    private String tempo;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "WORKOUT_RPE_MIN"
    )
    @DecimalMax(
            value = "10.0",
            inclusive = true,
            message = "WORKOUT_RPE_MAX"
    )
    private BigDecimal rpe;

    @Size(
            max = 5000,
            message = "WORKOUT_INSTRUCTION_TOO_LONG"
    )
    private String instruction;

    @Size(
            max = 5000,
            message = "WORKOUT_EXERCISE_NOTE_TOO_LONG"
    )
    private String note;

    @Size(
            max = 500,
            message = "WORKOUT_VIDEO_URL_TOO_LONG"
    )
    private String videoUrl;

    @Min(
            value = 0,
            message = "WORKOUT_SORT_ORDER_MIN"
    )
    private Integer sortOrder;

    @Builder.Default
    private Boolean isOptional = false;
}