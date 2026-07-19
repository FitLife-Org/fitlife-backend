package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.AiPlanItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPlanItemResponse {

    private Long id;

    private Long aiSuggestionId;

    private AiPlanItemType itemType;

    private String title;

    private String description;

    private Integer dayNo;

    private String dayOfWeek;

    private String exerciseName;

    private Integer sets;

    private String reps;

    private Integer restSeconds;

    private Integer durationMinutes;

    private String mealName;

    private String portionText;

    private Integer calories;

    private BigDecimal proteinGrams;

    private BigDecimal carbsGrams;

    private BigDecimal fatGrams;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}