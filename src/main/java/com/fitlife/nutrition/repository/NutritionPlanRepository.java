package com.fitlife.nutrition.repository;

import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionPlanRepository
        extends JpaRepository<NutritionPlan, Long> {

    Page<NutritionPlan>
    findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );

    Optional<NutritionPlan>
    findByIdAndMemberIdAndIsDeletedFalse(
            Long id,
            Long memberId
    );

    Optional<NutritionPlan>
    findByMemberIdAndStatusAndIsDeletedFalse(
            Long memberId,
            NutritionPlanStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT plan
            FROM NutritionPlan plan
            WHERE plan.member.id = :memberId
              AND plan.status = :status
              AND plan.isDeleted = false
            """)
    Optional<NutritionPlan>
    findByMemberIdAndStatusAndIsDeletedFalseForUpdate(
            @Param("memberId")
            Long memberId,

            @Param("status")
            NutritionPlanStatus status
    );

    Optional<NutritionPlan> findByAiSuggestionId(
            Long aiSuggestionId
    );

    boolean existsByAiSuggestionId(
            Long aiSuggestionId
    );

    Page<NutritionPlan>
    findByIsDeletedFalseOrderByCreatedAtDesc(
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM trainer_assignments
                    WHERE trainer_id = :trainerId
                      AND member_id = :memberId
                      AND status = 'ACTIVE'
                    """,
            nativeQuery = true
    )
    long countActiveTrainerAssignment(
            @Param("trainerId")
            Long trainerId,

            @Param("memberId")
            Long memberId
    );
}