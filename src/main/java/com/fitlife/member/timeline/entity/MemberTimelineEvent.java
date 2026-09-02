package com.fitlife.member.timeline.entity;

import com.fitlife.member.entity.Member;
import com.fitlife.member.timeline.enums.MemberTimelineType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member_timeline_events")
public class MemberTimelineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private MemberTimelineType type;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 1000)
    private String description;
    private Long referenceId;
    @Column(length = 60)
    private String referenceType;
    @Column(length = 60)
    private String status;
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        LocalDateTime n = LocalDateTime.now();
        if (occurredAt == null) occurredAt = n;
        if (createdAt == null) createdAt = n;
    }
}
