package com.fitlife.subscription.entity;

import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscriptions_member", columnList = "member_id"),
                @Index(name = "idx_subscriptions_package", columnList = "gym_package_id"),
                @Index(name = "idx_subscriptions_duration", columnList = "package_duration_id"),
                @Index(name = "idx_subscriptions_status", columnList = "status"),
                @Index(name = "idx_subscriptions_dates", columnList = "start_date, end_date")
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_package_id", nullable = false)
    private GymPackage gymPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_duration_id", nullable = false)
    private PackageDuration packageDuration;

    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "pt_sessions_total", nullable = false)
    private Integer ptSessionsTotal;

    @Column(name = "pt_sessions_used", nullable = false)
    private Integer ptSessionsUsed;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionStatus status;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = SubscriptionStatus.PENDING_PAYMENT;
        }

        if (autoRenew == null) {
            autoRenew = false;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (ptSessionsTotal == null) {
            ptSessionsTotal = 0;
        }

        if (ptSessionsUsed == null) {
            ptSessionsUsed = 0;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}