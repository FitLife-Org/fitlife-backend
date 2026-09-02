package com.fitlife.nutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "nutrition_plan_items",
        indexes = {
                @Index(
                        name = "idx_nutrition_plan_items_plan",
                        columnList = "nutrition_plan_id"
                ),
                @Index(
                        name = "idx_nutrition_plan_items_order",
                        columnList = "nutrition_plan_id, sort_order"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "nutrition_plan_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_nutrition_plan_items_plan"
            )
    )
    private NutritionPlan nutritionPlan;

    @Column(
            name = "meal_name",
            nullable = false,
            length = 150
    )
    private String mealName;

    @Column(
            name = "food_name",
            nullable = false,
            length = 200
    )
    private String foodName;

    @Column(
            precision = 10,
            scale = 2
    )
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(
            name = "portion_text",
            length = 255
    )
    private String portionText;

    private Integer calories;

    @Column(
            name = "protein_grams",
            precision = 8,
            scale = 2
    )
    private BigDecimal proteinGrams;

    @Column(
            name = "carbohydrate_grams",
            precision = 8,
            scale = 2
    )
    private BigDecimal carbohydrateGrams;

    @Column(
            name = "fat_grams",
            precision = 8,
            scale = 2
    )
    private BigDecimal fatGrams;

    @Column(length = 500)
    private String preparation;

    @Column(length = 500)
    private String substitution;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}