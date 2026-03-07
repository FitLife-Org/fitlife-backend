package com.fitlife.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String exercise_name; // Khớp với migration
    private Integer sets;
    private String reps;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // QUAN TRỌNG: Phải có trường này thì mới Check-in được
    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @ToString.Exclude
    @JsonBackReference // Chặn vòng lặp JSON
    private WorkoutSession session;
}