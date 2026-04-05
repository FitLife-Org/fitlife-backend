package com.fitlife.ai_workout.entity;

import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_workout_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWorkoutPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String goal;

    @Column(columnDefinition = "LONGTEXT")  // Store the AI-generated workout plan as a JSON string or plain text
    private String planData;

    private LocalDateTime createdAt;
}