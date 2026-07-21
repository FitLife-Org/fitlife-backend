package com.fitlife.nutrition.dto.response;

import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlanResponse {
    private Long id;
    private String name;
    private String description;
    private String goal;
    private NutritionPlanSource source;
    private NutritionPlanStatus status;
    private Integer durationWeeks;
    private Integer dailyCalories;
    private BigDecimal proteinGrams;
    private BigDecimal carbohydrateGrams;
    private BigDecimal fatGrams;
    private BigDecimal fiberGrams;
    private Integer mealsPerDay;
    private Integer waterMlPerDay;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String foodsToLimit;
    private String substitutionNote;
    private String trainerNote;
    private String memberNote;
    private String warningMessage;
    private Boolean modifiedFromAi;
    private LocalDateTime completedAt;
    private LocalDateTime archivedAt;
    private Long aiSuggestionId;
    private Long replacementPlanId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MealDto> meals;
}
