package com.fitlife.ai_workout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@NamedEntityGraph(
        name = "WorkoutPlan.fullGraph",
        attributeNodes = {
                @NamedAttributeNode(value = "sessions", subgraph = "sessions-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "sessions-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("details")
                        }
                )
        }
)
@Entity
@Table(name = "workout_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore // 1. FIX: Chặn lặp vô tận qua đường Member -> User -> Member
    private Member member;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String status; // ACTIVE, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    // 2. FIX: Xóa @JsonManagedReference đi, để Jackson tự quét bình thường
    private Set<WorkoutSession> sessions = new LinkedHashSet<>();

    public enum PlanStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}