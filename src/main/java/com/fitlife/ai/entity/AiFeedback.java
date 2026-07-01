package com.fitlife.ai.entity;

import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ai_feedbacks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_feedbacks_suggestion_member",
                        columnNames = {"ai_suggestion_id", "member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ai_feedbacks_member", columnList = "member_id"),
                @Index(name = "idx_ai_feedbacks_rating", columnList = "rating")
        }
)
public class AiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * AI suggestion được đánh giá.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_suggestion_id", nullable = false)
    private AiSuggestion aiSuggestion;

    /**
     * Member gửi feedback.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Rating từ 1 đến 5.
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}