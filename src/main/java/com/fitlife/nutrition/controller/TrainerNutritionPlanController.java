package com.fitlife.nutrition.controller;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.trainer.repository.TrainerRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Trainer Nutrition Plan API", description = "APIs for trainers to manage their assigned members' nutrition plans")
public class TrainerNutritionPlanController {

    private final TrainerNutritionPlanService trainerNutritionPlanService;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    private Long getTrainerId() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        com.fitlife.user.entity.User user = userRepository.findByUsernameOrEmail(authentication.getName(), authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        com.fitlife.trainer.entity.Trainer trainer = trainerRepository.findByUserIdAndDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
        return trainer.getId();
    }

    // =========================================================================
    // LEGACY ENDPOINTS (Query Param TrainerID)
    // =========================================================================

    @Operation(summary = "Trainer creates a new nutrition plan for a member")
    @PostMapping("/trainer/nutrition-plans/members/{memberId}")
    public ResponseEntity<NutritionPlanResponse> createPlan(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.createPlanForMember(trainerId, memberId, request));
    }

    @Operation(summary = "Trainer updates an existing DRAFT nutrition plan for a member")
    @PutMapping("/trainer/nutrition-plans/{planId}/members/{memberId}")
    public ResponseEntity<NutritionPlanResponse> updatePlan(
            @RequestParam Long trainerId,
            @PathVariable Long planId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.updatePlanForMember(trainerId, planId, memberId, request));
    }

    @Operation(summary = "Trainer views all nutrition plans of an assigned member")
    @GetMapping("/trainer/nutrition-plans/members/{memberId}")
    public ResponseEntity<org.springframework.data.domain.Page<NutritionPlanResponse>> getPlansForMember(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trainerNutritionPlanService.getPlansForMember(trainerId, memberId, pageable));
    }

    // =========================================================================
    // FRONTEND COMPATIBLE ENDPOINTS (Auto-Resolves TrainerID from Context)
    // =========================================================================

    @Operation(summary = "Trainer views all nutrition plans of an assigned member (frontend compatible)")
    @GetMapping("/trainer/members/{memberId}/nutrition-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<org.springframework.data.domain.Page<NutritionPlanResponse>> getTrainerPlans(
            @PathVariable Long memberId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trainerNutritionPlanService.getPlansForMember(getTrainerId(), memberId, pageable));
    }

    @Operation(summary = "Trainer creates a new nutrition plan for a member (frontend compatible)")
    @PostMapping("/trainer/members/{memberId}/nutrition-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<NutritionPlanResponse> createTrainerPlan(
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.createPlanForMember(getTrainerId(), memberId, request));
    }

    @Operation(summary = "Trainer updates an existing nutrition plan for a member (frontend compatible)")
    @PatchMapping("/trainer/members/{memberId}/nutrition-plans/{planId}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<NutritionPlanResponse> updateTrainerPlan(
            @PathVariable Long memberId,
            @PathVariable Long planId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.updatePlanForMember(getTrainerId(), planId, memberId, request));
    }
}
