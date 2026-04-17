package com.fitlife.workout.repository;

import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    @EntityGraph(value = "WorkoutPlan.fullGraph", type = EntityGraph.EntityGraphType.LOAD)
    List<WorkoutPlan> findByMemberAndStatus(Member member, String status);
}