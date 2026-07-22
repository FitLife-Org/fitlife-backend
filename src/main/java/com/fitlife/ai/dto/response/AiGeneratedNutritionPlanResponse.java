package com.fitlife.ai.dto.response;

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
public class AiGeneratedNutritionPlanResponse {

    private String summary;

    private String bodyAnalysis;

    private AiGeneratedNutritionResponse
            nutritionPlan;

    /**
     * Các cảnh báo an toàn do AI tạo.
     *
     * Orchestrator sẽ chuẩn hóa:
     * - loại bỏ null/rỗng;
     * - loại bỏ trùng lặp;
     * - giới hạn tối đa 2 cảnh báo.
     */
    @Builder.Default
    private List<String> warnings =
            new ArrayList<>();
}