package com.fitlife.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_plan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private NutritionPlan nutritionPlan;

    @Column(name = "meal_name", nullable = false, length = 100)
    private String mealName;

    @Column(name = "food_items", columnDefinition = "TEXT")
    private String foodItems;

    private Integer calories;

    @Column(name = "protein_grams")
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams")
    private BigDecimal carbsGrams;

    @Column(name = "fat_grams")
    private BigDecimal fatGrams;

    @Column(name = "is_customized", nullable = false)
    @Builder.Default
    private Boolean isCustomized = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}