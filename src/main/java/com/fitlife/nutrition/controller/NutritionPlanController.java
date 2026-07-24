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
@RequestMapping("/nutrition-plans/me")
@RequiredArgsConstructor
@Tag(
        name = "Member Nutrition Plan API",
        description = "APIs for members to manage their own nutrition plans"
)
@PreAuthorize("hasRole('MEMBER')")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @Operation(summary = "Get my nutrition plans")
    @GetMapping
    public ResponseEntity<Page<NutritionPlanResponse>> getMyPlans(
            Authentication authentication,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyNutritionPlans(
                        getPrincipal(authentication),
                        pageable
                )
        );
    }

    @Operation(summary = "Get my nutrition plan by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NutritionPlanResponse> getMyPlanById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyNutritionPlanById(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @Operation(summary = "Get my active nutrition plan")
    @GetMapping("/active")
    public ResponseEntity<NutritionPlanResponse> getMyActivePlan(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyActiveNutritionPlan(
                        getPrincipal(authentication)
                )
        );
    }

    @Operation(summary = "Get today's nutrition plan")
    @GetMapping("/today")
    public ResponseEntity<NutritionPlanResponse> getMyTodayPlan(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                nutritionPlanService.getMyTodayNutritionPlan(
                        getPrincipal(authentication)
                )
        );
    }

    @Operation(summary = "Create my nutrition plan")
    @PostMapping
    public ResponseEntity<NutritionPlanResponse> createMyPlan(
            Authentication authentication,
            @Valid @RequestBody NutritionPlanRequest request
    ) {
        NutritionPlanResponse response =
                nutritionPlanService.createMyNutritionPlan(
                        getPrincipal(authentication),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Update my DRAFT nutrition plan")
    @PutMapping("/{id}")
    public ResponseEntity<NutritionPlanResponse> updateMyPlan(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody NutritionPlanRequest request
    ) {
        return ResponseEntity.ok(
                nutritionPlanService.updateMyNutritionPlan(
                        id,
                        getPrincipal(authentication),
                        request
                )
        );
    }

    @Operation(summary = "Activate my nutrition plan")
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateMyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.activateMyNutritionPlan(
                id,
                getPrincipal(authentication)
        );

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Archive my nutrition plan")
    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveMyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.archiveMyNutritionPlan(
                id,
                getPrincipal(authentication)
        );

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Complete my active nutrition plan")
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeMyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.completeMyNutritionPlan(
                id,
                getPrincipal(authentication)
        );

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Clone my nutrition plan")
    @PostMapping("/{id}/clone")
    public ResponseEntity<NutritionPlanResponse> cloneMyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        NutritionPlanResponse response =
                nutritionPlanService.cloneMyNutritionPlan(
                        id,
                        getPrincipal(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Delete my nutrition plan")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        nutritionPlanService.deleteMyNutritionPlan(
                id,
                getPrincipal(authentication)
        );

        return ResponseEntity.noContent().build();
    }

    private String getPrincipal(
            Authentication authentication
    ) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(
                authentication.getName()
        )) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return authentication.getName();
    }
}