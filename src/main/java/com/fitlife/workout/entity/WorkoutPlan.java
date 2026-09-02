package com.fitlife.workout.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fitlife.workout.enums.WorkoutPlanSourceType;

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

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "trainer_id")
    private Long trainerId;

    @Column(
            name = "source_ai_suggestion_id",
            unique = true
    )
    private Long sourceAiSuggestionId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String goal;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "duration_weeks", nullable = false)
    @Builder.Default
    private Integer durationWeeks = 4;

    @Column(name = "workout_days_per_week", nullable = false)
    @Builder.Default
    private Integer workoutDaysPerWeek = 3;

    @Column(name = "workout_duration_minutes")
    private Integer workoutDurationMinutes;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "source_type",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private WorkoutPlanSourceType sourceType =
            WorkoutPlanSourceType.MANUAL;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "workoutPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<WorkoutPlanDay> days = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addDay(WorkoutPlanDay day) {
        days.add(day);
        day.setWorkoutPlan(this);
    }

    public void removeDay(WorkoutPlanDay day) {
        days.remove(day);
        day.setWorkoutPlan(null);
    }
}