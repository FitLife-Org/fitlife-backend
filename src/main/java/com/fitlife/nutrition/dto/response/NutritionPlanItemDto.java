package com.fitlife.nutrition.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
