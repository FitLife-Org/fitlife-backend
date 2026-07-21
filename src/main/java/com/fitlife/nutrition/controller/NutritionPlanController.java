package com.fitlife.nutrition.controller;

import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Nutrition Plan API", description = "APIs for managing member nutrition plans")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @Operation(summary = "Get a nutrition plan by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NutritionPlanResponse> getPlanById(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        return ResponseEntity.ok(nutritionPlanService.getNutritionPlanById(id, memberId));
    }

    @Operation(summary = "Get all nutrition plans for a member")
    @GetMapping
    public ResponseEntity<Page<NutritionPlanResponse>> getPlansByMember(
            @RequestParam Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(nutritionPlanService.getNutritionPlansByMember(memberId, pageable));
    }

    @Operation(summary = "Activate a nutrition plan", description = "Activates a plan and automatically archives any currently active plan for the member.")
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activatePlan(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        nutritionPlanService.activateNutritionPlan(id, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Archive a nutrition plan")
    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archivePlan(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        nutritionPlanService.archiveNutritionPlan(id, memberId);
        return ResponseEntity.ok().build();
    }
}