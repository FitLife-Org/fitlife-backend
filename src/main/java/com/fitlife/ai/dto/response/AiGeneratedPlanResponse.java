package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedPlanResponse {

    private String summary;

    /**
     * Full Plan hiện trả bodyAnalysis dạng text ngắn.
     */
    private String bodyAnalysis;

    @Builder.Default
    private List<AiGeneratedWorkoutDayResponse> workoutPlan =
            new ArrayList<>();

    private AiGeneratedNutritionResponse nutritionPlan;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}