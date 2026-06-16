package com.fitlife.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitlife.ai.entity.AiWorkoutPlan;
import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.BigDecimal;
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
    @JsonIgnore
    @ToString.Exclude
    private Member member;

    // PhĂ„â€Ă‚Â¡c Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă¢â‚¬Å“ dinh dÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă‚Â¡ng cĂ„â€Ă‚Â³ thÄ‚Â¡Ă‚Â»Ă†â€™ Ä‚â€Ă¢â‚¬ËœÄ‚â€ Ă‚Â°Ä‚Â¡Ă‚Â»Ă‚Â£c tÄ‚Â¡Ă‚ÂºĂ‚Â¡o Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă¢â€Â¢c lÄ‚Â¡Ă‚ÂºĂ‚Â­p hoÄ‚Â¡Ă‚ÂºĂ‚Â·c gÄ‚Â¡Ă‚ÂºĂ‚Â¯n liÄ‚Â¡Ă‚Â»Ă‚Ân vÄ‚Â¡Ă‚Â»Ă¢â‚¬Âºi 1 KÄ‚Â¡Ă‚ÂºĂ‚Â¿ hoÄ‚Â¡Ă‚ÂºĂ‚Â¡ch tÄ‚Â¡Ă‚ÂºĂ‚Â­p cÄ‚Â¡Ă‚Â»Ă‚Â§a AI
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_workout_plan_id")
    @JsonIgnore
    @ToString.Exclude
    private AiWorkoutPlan aiWorkoutPlan;

    @Column(name = "target_calories", nullable = false)
    private Integer targetCalories;

    @Column(name = "protein_grams")
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams")
    private BigDecimal carbsGrams;

    @Column(name = "fat_grams")
    private BigDecimal fatGrams;

    @Column(length = 50)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<NutritionPlanItem> meals = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}