package com.fitlife.workout.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanDayResponse {
    private Long id;
    private Integer weekNo;
    private Integer dayNo;
    private String dayOfWeek;
    private String name;
    private String focusArea;
    private Integer estimatedMinutes;
    private String note;
    private Integer sortOrder;
    private Boolean isRestDay;
    private List exercises;
}