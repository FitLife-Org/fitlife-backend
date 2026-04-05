package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.AiWorkoutPlan;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiWorkoutPlanRepository extends JpaRepository<AiWorkoutPlan, Long> {
    List<AiWorkoutPlan> findByMemberOrderByCreatedAtDesc(Member member);
}