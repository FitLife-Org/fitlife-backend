package com.fitlife.bodymetric.entity;

import com.fitlife.member.entity.Member;
import com.fitlife.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
                ),
                @Index(
                        name = "idx_body_metrics_member_deleted",
                        columnList = "member_id, is_deleted"
                ),
                @Index(
                        name = "idx_body_metrics_recorded_at",
                        columnList = "recorded_at"
                )
        }
)
public class BodyMetric {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    /**
     * User tạo bản ghi.
     *
     * Có thể là:
     * - chính Member;
     * - Admin;
     * - Staff.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(
            name = "weight_kg",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal weightKg;

    @Column(
            name = "height_cm",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal heightCm;

    @Column(
            name = "bmi",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal bmi;

    @Column(
            name = "body_fat_percent",
            precision = 5,
            scale = 2
    )
    private BigDecimal bodyFatPercent;

    @Column(
            name = "muscle_mass_kg",
            precision = 5,
            scale = 2
    )
    private BigDecimal muscleMassKg;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    private String note;

    @Column(
            name = "recorded_at",
            nullable = false
    )
    private LocalDateTime recordedAt;

    @Builder.Default
    @Column(
            name = "is_deleted",
            nullable = false
    )
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt =
                    LocalDateTime.now();
        }

        if (isDeleted == null) {
            isDeleted = false;
        }

        calculateBmi();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateBmi();
    }

    /**
     * BMI = weightKg / heightMeter².
     *
     * BMI tuyệt đối không nhận từ client.
     */
    public void calculateBmi() {
        if (
                weightKg == null ||
                        heightCm == null
        ) {
            throw new IllegalStateException(
                    "Weight and height are required to calculate BMI"
            );
        }

        if (
                weightKg.compareTo(
                        BigDecimal.ZERO
                ) <= 0 ||
                        heightCm.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            throw new IllegalStateException(
                    "Weight and height must be greater than zero"
            );
        }

        BigDecimal heightMeter =
                heightCm.divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP
                );

        BigDecimal heightSquare =
                heightMeter.multiply(
                        heightMeter
                );

        bmi = weightKg.divide(
                heightSquare,
                2,
                RoundingMode.HALF_UP
        );
    }
}