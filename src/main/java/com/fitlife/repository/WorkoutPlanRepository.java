package com.fitlife.repository;

import com.fitlife.entity.Member;
import com.fitlife.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    Optional<WorkoutPlan> findByMemberAndStatus(Member member, WorkoutPlan.PlanStatus status);
}