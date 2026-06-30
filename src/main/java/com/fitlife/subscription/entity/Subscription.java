package com.fitlife.subscription.entity;

import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

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
                @Index(name = "idx_subscriptions_status", columnList = "status"),
                @Index(name = "idx_subscriptions_dates", columnList = "start_date, end_date")
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Member đăng ký gói.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /*
     * Hiện tại chưa có PackageDuration nên vẫn dùng gym_package_id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_package_id", nullable = false)
    private GymPackage gymPackage;

    /*
     * Sau V5:
     * Khi mới tạo subscription: null
     * Khi payment SUCCESS: set startDate/endDate
     */
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

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}