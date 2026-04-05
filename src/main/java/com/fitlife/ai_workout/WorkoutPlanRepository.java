package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.WorkoutPlan;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    @EntityGraph(value = "WorkoutPlan.fullGraph", type = EntityGraph.EntityGraphType.LOAD)
    Optional<WorkoutPlan> findByMemberAndStatus(Member member, String status);
}