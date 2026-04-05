package com.fitlife.member.entity;

import com.fitlife.subscription.Subscription;
import com.fitlife.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Double height;
    private Double weight;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    public Subscription getActiveSubscription() {
        if (subscriptions == null) return null;
        return subscriptions.stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()))
                .findFirst()
                .orElse(null);
    }
}