package com.fitlife.nutrition.controller;

import com.fitlife.common.response.ApiResponse;
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

   @PostMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> createPlan(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Success", trainerNutritionPlanService.createPlanForMember(trainerId, memberId, request)));
    }

 @PutMapping("/{planId}/members/{memberId}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> updatePlan(
            @RequestParam Long trainerId,
            @PathVariable Long planId,
            @PathVariable Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Success", trainerNutritionPlanService.updatePlanForMember(trainerId, planId, memberId, request)));
    }

   @GetMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<NutritionPlanResponse>>> getPlansForMember(
            @RequestParam Long trainerId,
            @PathVariable Long memberId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Success", trainerNutritionPlanService.getPlansForMember(trainerId, memberId, pageable)));
    }
}
