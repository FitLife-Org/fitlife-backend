package com.fitlife.nutrition.entity;

import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.member.entity.Member;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_suggestion_id", unique = true)
    private AiSuggestion aiSuggestion;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NutritionPlanSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private NutritionPlanStatus status = NutritionPlanStatus.DRAFT;

    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @Column(name = "daily_calories")
    private Integer dailyCalories;

    @Column(name = "protein_grams", precision = 8, scale = 2)
    private BigDecimal proteinGrams;

    @Column(name = "carbohydrate_grams", precision = 8, scale = 2)
    private BigDecimal carbohydrateGrams;

    @Column(name = "fat_grams", precision = 8, scale = 2)
    private BigDecimal fatGrams;

    @Column(name = "fiber_grams", precision = 8, scale = 2)
    private BigDecimal fiberGrams;

    @Column(name = "meals_per_day")
    private Integer mealsPerDay;

    @Column(name = "water_ml_per_day")
    private Integer waterMlPerDay;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_plan_id")
    private NutritionPlan replacementPlan;

    @Column(name = "foods_to_limit", columnDefinition = "TEXT")
    private String foodsToLimit;

    @Column(name = "substitution_note", columnDefinition = "TEXT")
    private String substitutionNote;

    @Column(name = "trainer_note", columnDefinition = "TEXT")
    private String trainerNote;

    @Column(name = "member_note", columnDefinition = "TEXT")
    private String memberNote;

    @Column(name = "warning_message", columnDefinition = "TEXT")
    private String warningMessage;

    @Column(name = "modified_from_ai", nullable = false)
    @Builder.Default
    private Boolean modifiedFromAi = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NutritionPlanItem> items = new ArrayList<>();
}
