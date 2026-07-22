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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/nutrition-plans")
@RequiredArgsConstructor
@Tag(name = "Admin Nutrition Plan Management API", description = "Endpoints for administrators to monitor and manage all nutrition plans")
public class AdminNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Admin views all nutrition plans in the system")
    @GetMapping
    public ResponseEntity<Page<NutritionPlanResponse>> getAllPlans(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(nutritionPlanService.getAllNutritionPlansForAdmin(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Admin views details of any nutrition plan")
    @GetMapping("/{id}")
    public ResponseEntity<NutritionPlanResponse> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(nutritionPlanService.getNutritionPlanByIdForAdmin(id));
    }
}
