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
                @Index(
                        name = "idx_ai_plan_items_suggestion",
                        columnList = "ai_suggestion_id"
                ),
                @Index(
                        name = "idx_ai_plan_items_type",
                        columnList = "item_type"
                ),
                @Index(
                        name = "idx_ai_plan_items_sort",
                        columnList = "ai_suggestion_id, sort_order"
                )
        }
)
public class AiPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Suggestion sở hữu item này.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ai_suggestion_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ai_plan_items_suggestion"
            )
    )
    private AiSuggestion aiSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "item_type",
            nullable = false,
            length = 50
    )
    private AiPlanItemType itemType;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    /**
     * Ngày số mấy trong kế hoạch: 1, 2, 3...
     */
    @Column(name = "day_no")
    private Integer dayNo;

    /**
     * MONDAY, TUESDAY...
     *
     * Hiện giữ String để tương thích parser.
     * Có thể chuyển enum DayOfWeek ở giai đoạn sau.
     */
    @Column(
            name = "day_of_week",
            length = 30
    )
    private String dayOfWeek;

    @Column(
            name = "exercise_name",
            length = 150
    )
    private String exerciseName;

    @Column(name = "sets")
    private Integer sets;

    /**
     * Có thể chứa:
     * - "8-12"
     * - "10 mỗi bên"
     * - "AMRAP"
     */
    @Column(
            name = "reps",
            length = 50
    )
    private String reps;

    /**
     * Thời gian nghỉ giữa các hiệp.
     */
    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(
            name = "meal_name",
            length = 100
    )
    private String mealName;

    /**
     * Mô tả khẩu phần ngắn.
     *
     * Ví dụ:
     * 150g ức gà + 200g cơm + rau xanh
     */
    @Column(
            name = "portion_text",
            length = 255
    )
    private String portionText;

    @Column(name = "calories")
    private Integer calories;

    @Column(
            name = "protein_grams",
            precision = 7,
            scale = 2
    )
    private BigDecimal proteinGrams;

    @Column(
            name = "carbs_grams",
            precision = 7,
            scale = 2
    )
    private BigDecimal carbsGrams;

    @Column(
            name = "fat_grams",
            precision = 7,
            scale = 2
    )
    private BigDecimal fatGrams;

    @Builder.Default
    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder = 0;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
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