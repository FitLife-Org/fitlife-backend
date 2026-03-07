package com.fitlife.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Đại diện cho một buổi tập trong ngày (ví dụ: Thứ 2 tập Ngực)
 */
@Entity
@Table(name = "workout_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek; // Thứ 2, Thứ 3...
    private String focusArea; // Ngực, Tay sau...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @ToString.Exclude // Tránh lỗi vòng lặp khi log dữ liệu
    @JsonBackReference
    private WorkoutPlan workoutPlan;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WorkoutDetail> details;
}