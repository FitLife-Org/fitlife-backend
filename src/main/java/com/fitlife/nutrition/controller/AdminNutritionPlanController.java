package com.fitlife.nutrition.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Admin Nutrition Plan Management API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @GetMapping
    @Operation(summary = "Admin views all nutrition plans")
    public ApiResponse<PageResponse<NutritionPlanResponse>> getAllPlans(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(
                "Get nutrition plans successfully",
                PageResponse.from(nutritionPlanService.getAllNutritionPlansForAdmin(pageable))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Admin views nutrition plan detail")
    public ApiResponse<NutritionPlanResponse> getPlanById(@PathVariable Long id) {
        return ApiResponse.success(
                "Get nutrition plan successfully",
                nutritionPlanService.getNutritionPlanByIdForAdmin(id)
        );
    }
}
