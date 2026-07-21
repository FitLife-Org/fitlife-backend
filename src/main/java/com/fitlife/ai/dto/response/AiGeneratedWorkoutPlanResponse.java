package com.fitlife.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Kết quả AI dành riêng cho Workout Plan.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedWorkoutPlanResponse {

    private String summary;

    private String bodyAnalysis;

    private List<AiGeneratedWorkoutDayResponse> workoutPlan;

    private List<String> warnings;
}