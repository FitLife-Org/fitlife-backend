package com.fitlife.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workout_plans")
@Data
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
    private Member member;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private PlanStatus status; // ACTIVE, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<WorkoutSession> sessions;

    public enum PlanStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}