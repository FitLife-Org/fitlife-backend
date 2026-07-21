package com.fitlife.workout.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanDayRequest {
    private Integer weekNo;
    private Integer dayNo;
    private String dayOfWeek;
    private String name;
    private String focusArea;
    private Integer estimatedMinutes;
    private String note;
    private Integer sortOrder;
    private Boolean isRestDay;
    private List<WorkoutExerciseRequest> exercises;

    public List<WorkoutExerciseRequest> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExerciseRequest> exercises) {
        this.exercises = exercises;
    }
}