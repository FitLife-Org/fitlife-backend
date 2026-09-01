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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Trainer Nutrition Plan API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TRAINER')")
public class TrainerNutritionPlanController {

    private final TrainerNutritionPlanService trainerNutritionPlanService;

    private String getTrainerUsername() {
        Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
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

    // =========================================================================
    // LEGACY ENDPOINTS (Query Param TrainerID)
    // =========================================================================

    @Operation(summary = "Trainer creates a new nutrition plan for a member")
    @PostMapping("/trainer/nutrition-plans/members/{memberId}")
    public ApiResponse<NutritionPlanResponse> createPlan(
            @RequestParam(required = false) Long trainerId,
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

    @Operation(summary = "Trainer updates an existing DRAFT nutrition plan for a member")
    @PutMapping("/trainer/nutrition-plans/{planId}/members/{memberId}")
    public ApiResponse<NutritionPlanResponse> updatePlan(
            @RequestParam(required = false) Long trainerId,
            @PathVariable Long planId,
            @PathVariable Long memberId,
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

    @Operation(summary = "Trainer views all nutrition plans of an assigned member")
    @GetMapping("/trainer/nutrition-plans/members/{memberId}")
    public ApiResponse<PageResponse<NutritionPlanResponse>> getPlansForMember(
            @RequestParam(required = false) Long trainerId,
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

    // =========================================================================
    // FRONTEND COMPATIBLE ENDPOINTS (Auto-Resolves TrainerID from Context)
    // =========================================================================

    @Operation(summary = "Trainer views all nutrition plans of an assigned member (frontend compatible)")
    @GetMapping("/trainer/members/{memberId}/nutrition-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<org.springframework.data.domain.Page<NutritionPlanResponse>> getTrainerPlans(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(trainerNutritionPlanService.getPlansForMember(getTrainerUsername(), memberId, pageable));
    }

    @Operation(summary = "Trainer creates a new nutrition plan for a member (frontend compatible)")
    @PostMapping("/trainer/members/{memberId}/nutrition-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<NutritionPlanResponse> createTrainerPlan(
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.createPlanForMember(getTrainerUsername(), memberId, request));
    }

    @Operation(summary = "Trainer updates an existing nutrition plan for a member (frontend compatible)")
    @PatchMapping("/trainer/members/{memberId}/nutrition-plans/{planId}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<NutritionPlanResponse> updateTrainerPlan(
            @PathVariable Long memberId,
            @PathVariable Long planId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.updatePlanForMember(getTrainerUsername(), planId, memberId, request));
    }
}
