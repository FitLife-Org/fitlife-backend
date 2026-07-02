package com.fitlife.ai.entity;

import com.fitlife.ai.enums.AiPlanItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ai_plan_items",
        indexes = {
                @Index(name = "idx_ai_plan_items_suggestion", columnList = "ai_suggestion_id"),
                @Index(name = "idx_ai_plan_items_type", columnList = "item_type"),
                @Index(name = "idx_ai_plan_items_sort", columnList = "ai_suggestion_id, sort_order")
        }
)
public class AiPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Thuộc về một AI suggestion.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_suggestion_id", nullable = false)
    private AiSuggestion aiSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private AiPlanItemType itemType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Ngày thứ mấy trong plan: 1, 2, 3...
     */
    @Column(name = "day_no")
    private Integer dayNo;

    /**
     * MONDAY, TUESDAY...
     */
    @Column(name = "day_of_week", length = 30)
    private String dayOfWeek;

    @Column(name = "exercise_name", length = 150)
    private String exerciseName;

    @Column(name = "sets")
    private Integer sets;

    @Column(name = "reps", length = 50)
    private String reps;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "meal_name", length = 100)
    private String mealName;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein_grams", precision = 7, scale = 2)
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams", precision = 7, scale = 2)
    private BigDecimal carbsGrams;

    @Column(name = "fat_grams", precision = 7, scale = 2)
    private BigDecimal fatGrams;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}