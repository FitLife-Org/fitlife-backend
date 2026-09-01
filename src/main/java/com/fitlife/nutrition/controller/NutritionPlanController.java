package com.fitlife.nutrition.controller;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.NutritionPlanService;
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
@RequestMapping("/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Nutrition Plan API", description = "Member self-service nutrition plan APIs")
@SecurityRequirement(name = "bearerAuth")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Create a manual nutrition plan")
    public ApiResponse<NutritionPlanResponse> createMyNutritionPlan(
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication
    ) {
        NutritionPlanResponse response = nutritionPlanService.createMyNutritionPlan(
                getPrincipal(authentication),
                request
        );
        return ApiResponse.created("Create nutrition plan successfully", response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my nutrition plans")
    public ApiResponse<PageResponse<NutritionPlanResponse>> getMyNutritionPlans(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(
                "Get nutrition plans successfully",
                PageResponse.from(
                        nutritionPlanService.getMyNutritionPlans(
                                getPrincipal(authentication),
                                pageable
                        )
                )
        );
    }

    @GetMapping("/me/active")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my active nutrition plan")
    public ApiResponse<NutritionPlanResponse> getMyActiveNutritionPlan(Authentication authentication) {
        return ApiResponse.success(
                "Get active nutrition plan successfully",
                nutritionPlanService.getMyActiveNutritionPlan(getPrincipal(authentication))
        );
    }

    @GetMapping("/me/today")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get today's nutrition plan")
    public ApiResponse<NutritionPlanResponse> getMyTodayNutritionPlan(Authentication authentication) {
        return ApiResponse.success(
                "Get today's nutrition plan successfully",
                nutritionPlanService.getMyTodayNutritionPlan(getPrincipal(authentication))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my nutrition plan detail")
    public ApiResponse<NutritionPlanResponse> getMyNutritionPlanById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get nutrition plan successfully",
                nutritionPlanService.getMyNutritionPlanById(id, getPrincipal(authentication))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Update my DRAFT nutrition plan")
    public ApiResponse<NutritionPlanResponse> updateMyNutritionPlan(
            @PathVariable Long id,
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Update nutrition plan successfully",
                nutritionPlanService.updateMyNutritionPlan(id, getPrincipal(authentication), request)
        );
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<Void> activateMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.activateMyNutritionPlan(id, getPrincipal(authentication));
        return ApiResponse.success("Activate nutrition plan successfully", null);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<Void> completeMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.completeMyNutritionPlan(id, getPrincipal(authentication));
        return ApiResponse.success("Complete nutrition plan successfully", null);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<Void> archiveMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.archiveMyNutritionPlan(id, getPrincipal(authentication));
        return ApiResponse.success("Archive nutrition plan successfully", null);
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<NutritionPlanResponse> cloneMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.created(
                "Clone nutrition plan successfully",
                nutritionPlanService.cloneMyNutritionPlan(id, getPrincipal(authentication))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<Void> deleteMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.deleteMyNutritionPlan(id, getPrincipal(authentication));
        return ApiResponse.success("Delete nutrition plan successfully", null);
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
