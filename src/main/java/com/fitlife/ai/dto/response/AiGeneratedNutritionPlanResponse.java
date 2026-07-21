package com.fitlife.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedNutritionPlanResponse {

    private String summary;
    private String bodyAnalysis;
    private AiGeneratedNutritionResponse nutritionPlan;
    private List<String> warnings;
}