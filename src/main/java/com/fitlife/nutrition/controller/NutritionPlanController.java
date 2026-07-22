package com.fitlife.nutrition.controller;

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

    @Operation(summary = "Create a new nutrition plan")
    @PostMapping
    public ResponseEntity<NutritionPlanResponse> createPlan(
            @RequestParam Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(nutritionPlanService.createNutritionPlan(memberId, request));
    }

    @Operation(summary = "Update an existing DRAFT nutrition plan")
    @PutMapping("/{id}")
    public ResponseEntity<NutritionPlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestParam Long memberId,
            @Valid @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(nutritionPlanService.updateNutritionPlan(id, memberId, request));
    }

    @Operation(summary = "Delete a nutrition plan (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        nutritionPlanService.deleteNutritionPlan(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current active nutrition plan for a member")
    @GetMapping("/me/active")
    public ResponseEntity<NutritionPlanResponse> getActivePlan(
            @RequestParam Long memberId) {
        return ResponseEntity.ok(nutritionPlanService.getActiveNutritionPlan(memberId));
    }

    @Operation(summary = "Get today's nutrition plan for a member")
    @GetMapping("/me/today")
    public ResponseEntity<NutritionPlanResponse> getTodayPlan(
            @RequestParam Long memberId) {
        return ResponseEntity.ok(nutritionPlanService.getTodayNutritionPlan(memberId));
    }

    @Operation(summary = "Complete an active nutrition plan")
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completePlan(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        nutritionPlanService.completeNutritionPlan(id, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Clone an existing nutrition plan to a new draft plan")
    @PostMapping("/{id}/clone")
    public ResponseEntity<NutritionPlanResponse> clonePlan(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        return ResponseEntity.ok(nutritionPlanService.cloneNutritionPlan(id, memberId));
    }
}