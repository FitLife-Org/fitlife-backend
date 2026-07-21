package com.fitlife.workout.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_plan_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(name = "week_no", nullable = false)
    @Builder.Default
    private Integer weekNo = 1;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "focus_area", length = 150)
    private String focusArea;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_rest_day", nullable = false)
    @Builder.Default
    private Boolean isRestDay = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "workoutPlanDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<WorkoutExercise> exercises = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addExercise(WorkoutExercise exercise) {
        exercises.add(exercise);
        exercise.setWorkoutPlanDay(this);
    }

    public void removeExercise(WorkoutExercise exercise) {
        exercises.remove(exercise);
        exercise.setWorkoutPlanDay(null);
    }
}