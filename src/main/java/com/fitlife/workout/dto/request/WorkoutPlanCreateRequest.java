package com.fitlife.workout.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanCreateRequest {
    private Long memberId;
    private String name;
    private String goal;
    private String experienceLevel;
    private Integer durationWeeks;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;
    private String description;
    private String note;
    private List<WorkoutPlanDayRequest> days;

    public List<WorkoutPlanDayRequest> getDays() {
        return days;
    }

    public void setDays(List<WorkoutPlanDayRequest> days) {
        this.days = days;
    }
}