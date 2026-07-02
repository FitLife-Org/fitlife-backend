package com.fitlife.gympackage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gym_packages")
public class GymPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "package_type", nullable = false, length = 50)
    private String packageType;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "has_ai_workout_plan", nullable = false)
    @Builder.Default
    private Boolean hasAiWorkoutPlan = false;

    @Column(name = "has_nutrition_plan", nullable = false)
    @Builder.Default
    private Boolean hasNutritionPlan = false;

    @Column(name = "pt_sessions_per_month", nullable = false)
    @Builder.Default
    private Integer ptSessionsPerMonth = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
