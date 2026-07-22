package com.fitlife.nutrition.repository;

import com.fitlife.nutrition.entity.NutritionPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionPlanItemRepository
        extends JpaRepository<NutritionPlanItem, Long> {

    List<NutritionPlanItem>
    findByNutritionPlanIdOrderBySortOrderAscIdAsc(
            Long nutritionPlanId
    );

    void deleteByNutritionPlanId(
            Long nutritionPlanId
    );
}