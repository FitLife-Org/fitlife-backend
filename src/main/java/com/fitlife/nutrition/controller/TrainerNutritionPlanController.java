package com.fitlife.nutrition.controller;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainer/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Trainer Nutrition Plan API", description = "APIs for trainers to manage their assigned members' nutrition plans")
public class TrainerNutritionPlanController {

    private final TrainerNutritionPlanService trainerNutritionPlanService;

    @Operation(summary = "Trainer creates a new nutrition plan for a member")
    @PostMapping("/members/{memberId}")
    public ResponseEntity<NutritionPlanResponse> createPlan(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.createPlanForMember(trainerId, memberId, request));
    }

    @Operation(summary = "Trainer updates an existing DRAFT nutrition plan for a member")
    @PutMapping("/{planId}/members/{memberId}")
    public ResponseEntity<NutritionPlanResponse> updatePlan(
            @RequestParam Long trainerId,
            @PathVariable Long planId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(trainerNutritionPlanService.updatePlanForMember(trainerId, planId, memberId, request));
    }

    @Operation(summary = "Trainer views all nutrition plans of an assigned member")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<org.springframework.data.domain.Page<NutritionPlanResponse>> getPlansForMember(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trainerNutritionPlanService.getPlansForMember(trainerId, memberId, pageable));
    }
}
