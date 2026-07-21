package com.fitlife.nutrition.repository;

import com.fitlife.nutrition.entity.NutritionPlan;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {


    Page<NutritionPlan> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Optional<NutritionPlan> findByIdAndMemberIdAndIsDeletedFalse(Long id, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NutritionPlan> findByMemberIdAndStatusAndIsDeletedFalse(Long memberId, com.fitlife.nutrition.enums.NutritionPlanStatus status);
    
    Optional<NutritionPlan> findByAiSuggestionIdAndIsDeletedFalse(Long aiSuggestionId);
}
