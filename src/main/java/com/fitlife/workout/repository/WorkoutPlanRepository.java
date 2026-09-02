package com.fitlife.workout.repository;

import com.fitlife.workout.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository
        extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByMemberIdAndIsDeletedFalse(
            Long memberId
    );

    List<WorkoutPlan> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long memberId
    );

    Optional<WorkoutPlan> findFirstByMemberIdAndStatusAndIsDeletedFalse(
            Long memberId,
            String status
    );

    Optional<WorkoutPlan> findByIdAndMemberIdAndIsDeletedFalse(
            Long id,
            Long memberId
    );

    List<WorkoutPlan> findByMemberIdAndStatusAndIsDeletedFalse(
            Long memberId,
            String status
    );

    List<WorkoutPlan> findByIsDeletedFalseOrderByCreatedAtDesc();

    boolean existsBySourceAiSuggestionIdAndIsDeletedFalse(
            Long sourceAiSuggestionId
    );

    Optional<WorkoutPlan> findBySourceAiSuggestionIdAndIsDeletedFalse(
            Long sourceAiSuggestionId
    );
}
