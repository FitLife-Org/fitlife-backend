package com.fitlife.nutrition.controller;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainer/members/{memberId}/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Trainer Nutrition Plan API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TRAINER')")
public class TrainerNutritionPlanController {

    private final TrainerNutritionPlanService trainerNutritionPlanService;

    @PostMapping
    @Operation(summary = "Trainer creates nutrition plan for assigned member")
    public ApiResponse<NutritionPlanResponse> createPlan(
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication
    ) {
        return ApiResponse.created(
                "Create nutrition plan successfully",
                trainerNutritionPlanService.createPlanForMember(
                        getPrincipal(authentication),
                        memberId,
                        request
                )
        );
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Trainer updates assigned member DRAFT nutrition plan")
    public ApiResponse<NutritionPlanResponse> updatePlan(
            @PathVariable Long memberId,
            @PathVariable Long planId,
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Update nutrition plan successfully",
                trainerNutritionPlanService.updatePlanForMember(
                        getPrincipal(authentication),
                        planId,
                        memberId,
                        request
                )
        );
    }

    @GetMapping
    @Operation(summary = "Trainer views nutrition plans of assigned member")
    public ApiResponse<PageResponse<NutritionPlanResponse>> getPlansForMember(
            @PathVariable Long memberId,
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(
                "Get nutrition plans successfully",
                PageResponse.from(
                        trainerNutritionPlanService.getPlansForMember(
                                getPrincipal(authentication),
                                memberId,
                                pageable
                        )
                )
        );
    }

    private String getPrincipal(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())
                || "anonymous".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
