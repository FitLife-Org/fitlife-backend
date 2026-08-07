package com.fitlife.nutrition.controller;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Nutrition Plan API", description = "APIs for managing member nutrition plans")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @Operation(summary = "Create a new nutrition plan")
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> createMyNutritionPlan(
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication) {
        NutritionPlanResponse response = nutritionPlanService.createMyNutritionPlan(
                getPrincipal(authentication), request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all nutrition plans for current logged in member")
    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Page<NutritionPlanResponse>> getMyNutritionPlans(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyNutritionPlans(getPrincipal(authentication), pageable)
        );
    }

    @Operation(summary = "Get current active nutrition plan for a member")
    @GetMapping("/me/active")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> getMyActiveNutritionPlan(
            Authentication authentication) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyActiveNutritionPlan(getPrincipal(authentication))
        );
    }

    @Operation(summary = "Get today's nutrition plan for a member")
    @GetMapping("/me/today")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> getMyTodayNutritionPlan(
            Authentication authentication) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyTodayNutritionPlan(getPrincipal(authentication))
        );
    }

    @Operation(summary = "Get a nutrition plan by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> getMyNutritionPlanById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyNutritionPlanById(id, getPrincipal(authentication))
        );
    }

    @Operation(summary = "Update an existing nutrition plan")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> updateMyNutritionPlan(
            @PathVariable Long id,
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                nutritionPlanService.updateMyNutritionPlan(id, getPrincipal(authentication), request)
        );
    }

    @Operation(summary = "Update structure of an existing nutrition plan")
    @PutMapping("/{id}/structure")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> updateMyNutritionPlanStructure(
            @PathVariable Long id,
            @Valid @RequestBody NutritionPlanRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                nutritionPlanService.updateMyNutritionPlan(id, getPrincipal(authentication), request)
        ));
    }

    @Operation(summary = "Activate a nutrition plan")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> activateMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication) {
        nutritionPlanService.activateMyNutritionPlan(id, getPrincipal(authentication));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Complete an active nutrition plan")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> completeMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication) {
        nutritionPlanService.completeMyNutritionPlan(id, getPrincipal(authentication));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Archive a nutrition plan")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> archiveMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication) {
        nutritionPlanService.archiveMyNutritionPlan(id, getPrincipal(authentication));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Clone an existing nutrition plan to a new draft plan")
    @PostMapping("/{id}/clone")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<NutritionPlanResponse> cloneMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication) {
        NutritionPlanResponse response = nutritionPlanService.cloneMyNutritionPlan(
                id, getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete a nutrition plan (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> deleteMyNutritionPlan(
            @PathVariable Long id,
            Authentication authentication) {
        nutritionPlanService.deleteMyNutritionPlan(id, getPrincipal(authentication));
        return ResponseEntity.noContent().build();
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