package com.fitlife.workout.repository;

import com.fitlife.workout.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByMemberIdAndIsDeletedFalse(Long memberId);

    List<WorkoutPlan> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId);

    Optional findFirstByMemberIdAndStatusAndIsDeletedFalse(Long memberId, String status);

    Optional findByIdAndMemberIdAndIsDeletedFalse(Long id, Long memberId);

    List findByMemberIdAndStatusAndIsDeletedFalse(Long memberId, String status);



}