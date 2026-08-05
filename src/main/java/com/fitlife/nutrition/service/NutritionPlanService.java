package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NutritionPlanService {



    Page<NutritionPlanResponse> getMyNutritionPlans(
            String principal,
            Pageable pageable
    );

    NutritionPlanResponse getMyNutritionPlanById(
            Long planId,
            String principal
    );

    NutritionPlanResponse getMyActiveNutritionPlan(
            String principal
    );

    NutritionPlanResponse getMyTodayNutritionPlan(
            String principal
    );

    NutritionPlanResponse createMyNutritionPlan(
            String principal,
            NutritionPlanRequest request
    );

    NutritionPlanResponse updateMyNutritionPlan(
            Long planId,
            String principal,
            NutritionPlanRequest request
    );

    void deleteMyNutritionPlan(
            Long planId,
            String principal
    );

    void activateMyNutritionPlan(
            Long planId,
            String principal
    );

    void archiveMyNutritionPlan(
            Long planId,
            String principal
    );

    void completeMyNutritionPlan(
            Long planId,
            String principal
    );

    NutritionPlanResponse cloneMyNutritionPlan(
            Long planId,
            String principal
    );

    /*
     * =========================================================
     * INTERNAL — Trainer/Admin thao tác cho Member cụ thể
     * Chỉ được gọi sau khi đã kiểm tra quyền.
     * =========================================================
     */

    Page<NutritionPlanResponse> getNutritionPlansForMember(
            Long memberId,
            Pageable pageable
    );

    NutritionPlanResponse getNutritionPlanForMemberById(
            Long planId,
            Long memberId
    );

    NutritionPlanResponse createNutritionPlanForMember(
            Long memberId,
            NutritionPlanRequest request
    );

    NutritionPlanResponse updateNutritionPlanForMember(
            Long planId,
            Long memberId,
            NutritionPlanRequest request
    );

    /*
     * =========================================================
     * ADMIN
     * =========================================================
     */

    Page<NutritionPlanResponse> getAllNutritionPlansForAdmin(
            Pageable pageable
    );

    NutritionPlanResponse getNutritionPlanByIdForAdmin(
            Long id
    );
}