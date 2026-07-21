package com.fitlife.workout.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutExerciseRequest {
    private String exerciseName;
    private String targetMuscle;
    private Long equipmentId;
    private Integer sets;
    private String reps;
    private BigDecimal weightKg;
    private Integer durationMinutes;
    private BigDecimal distanceKm;
    private Integer restSeconds;
    private String tempo;
    private BigDecimal rpe;
    private String instruction;
    private String note;
    private String videoUrl;
    private Integer sortOrder;
    private Boolean isOptional;
}