package com.fitlife.member.entity;

import com.fitlife.auth.entity.User;
import com.fitlife.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "member_code", nullable = false, unique = true, length = 50)
    private String memberCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "height_cm")
    private BigDecimal height;

    @Column(name = "weight_kg")
    private BigDecimal weight;

    @Column(name = "bmi")
    private BigDecimal bmi;

    @Column(name = "fitness_goal", length = 100)
    private String fitnessGoal;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private transient List<Subscription> subscriptions;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Subscription getActiveSubscription() {
        if (subscriptions == null) {
            return null;
        }
        return subscriptions.stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()))
                .findFirst()
                .orElse(null);
    }
}