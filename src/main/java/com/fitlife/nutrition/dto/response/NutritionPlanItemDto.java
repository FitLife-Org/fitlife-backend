package com.fitlife.nutrition.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlanItemDto {
    private Long id;
    private String foodName;
    private BigDecimal quantity;
    private String unit;
    private String portionText;
    private Integer calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbohydrateGrams;
    private BigDecimal fatGrams;
    private String preparation;
    private String substitution;
    private String note;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
