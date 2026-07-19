package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedMealResponse {

    private String mealName;

    /**
     * Danh sách món ăn dạng text ngắn.
     */
    private String foodItems;

    /**
     * Khẩu phần cụ thể.
     *
     * Ví dụ:
     * 150g ức gà, 200g cơm, 200g rau.
     */
    private String portionText;

    private Integer calories;

    private BigDecimal proteinGrams;

    private BigDecimal carbsGrams;

    private BigDecimal fatGrams;

    private String note;
}