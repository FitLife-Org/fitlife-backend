package com.fitlife.bodymetric.entity;

import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "body_metrics",
        indexes = {
                @Index(
                        name = "idx_body_metrics_member_recorded",
                        columnList = "member_id, recorded_at"
                )
        }
)
public class BodyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Member sở hữu chỉ số cơ thể.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "bmi", precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "body_fat_percent", precision = 5, scale = 2)
    private BigDecimal bodyFatPercent;

    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (recordedAt == null) {
            recordedAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        calculateBmiIfPossible();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateBmiIfPossible();
    }

    public void calculateBmiIfPossible() {
        if (weightKg == null || heightCm == null) {
            return;
        }

        if (heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal heightMeter = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquare = heightMeter.multiply(heightMeter);

        if (heightSquare.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.bmi = weightKg.divide(heightSquare, 2, RoundingMode.HALF_UP);
    }
}