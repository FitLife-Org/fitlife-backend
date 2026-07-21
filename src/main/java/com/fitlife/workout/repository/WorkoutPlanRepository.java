package com.fitlife.workout.repository;

import com.fitlife.workout.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByMemberIdAndIsDeletedFalse(Long memberId);

    List<WorkoutPlan> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId);
}