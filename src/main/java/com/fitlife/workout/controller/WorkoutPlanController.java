package com.fitlife.workout.controller;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanDayRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;
import com.fitlife.workout.service.WorkoutPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    // =========================================================
    // MEMBER
    // =========================================================

    @PostMapping("/workout-plans")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response =
                workoutPlanService.createWorkoutPlan(
                        request,
                        getPrincipal(authentication)
                );

        return ApiResponse.created(
                "Create workout plan successfully",
                response
        );
    }

    @GetMapping("/workout-plans/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<List<WorkoutPlanResponse>> getMyWorkoutPlans(
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get workout plans successfully",
                workoutPlanService.getMyWorkoutPlans(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/me/active")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanDetailResponse> getActiveWorkoutPlan(
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get active workout plan successfully",
                workoutPlanService.getActiveWorkoutPlan(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/me/today")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanDayResponse> getTodayWorkoutDay(
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get today's workout successfully",
                workoutPlanService.getTodayWorkoutDay(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanDetailResponse> getWorkoutPlanById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get workout plan detail successfully",
                workoutPlanService.getWorkoutPlanById(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PatchMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> patchWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Update workout plan successfully",
                workoutPlanService.patchWorkoutPlan(
                        id,
                        request,
                        getPrincipal(authentication)
                )
        );
    }

    @PutMapping("/workout-plans/{id}/structure")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanDetailResponse> updateWorkoutPlanStructure(
            @PathVariable Long id,
            @Valid @RequestBody List<WorkoutPlanDayRequest> daysRequest,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Update workout plan structure successfully",
                workoutPlanService.updateWorkoutPlanStructure(
                        id,
                        daysRequest,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/activate")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> activateWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Activate workout plan successfully",
                workoutPlanService.activateWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/complete")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> completeWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Complete workout plan successfully",
                workoutPlanService.completeWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> archiveWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Archive workout plan successfully",
                workoutPlanService.archiveWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/clone")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<WorkoutPlanResponse> cloneWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        WorkoutPlanResponse response =
                workoutPlanService.cloneWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                );

        return ApiResponse.created(
                "Clone workout plan successfully",
                response
        );
    }

    // =========================================================
    // TRAINER
    // =========================================================

    @PostMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<WorkoutPlanResponse> createWorkoutPlanForMember(
            @PathVariable Long memberId,
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response =
                workoutPlanService.createWorkoutPlanForMember(
                        memberId,
                        request,
                        getPrincipal(authentication)
                );

        return ApiResponse.created(
                "Create workout plan for member successfully",
                response
        );
    }

    @GetMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<List<WorkoutPlanResponse>>
    getMemberWorkoutPlansForTrainer(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Get member workout plans successfully",
                workoutPlanService.getMemberWorkoutPlansForTrainer(
                        memberId,
                        getPrincipal(authentication)
                )
        );
    }

    @PatchMapping("/trainer/members/{memberId}/workout-plans/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<WorkoutPlanResponse> patchWorkoutPlanForMember(
            @PathVariable Long memberId,
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Update member workout plan successfully",
                workoutPlanService.patchWorkoutPlanForMember(
                        memberId,
                        id,
                        request,
                        getPrincipal(authentication)
                )
        );
    }

    // =========================================================
    // ADMIN
    // =========================================================

    @GetMapping("/admin/workout-plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<WorkoutPlanResponse>>
    getAllWorkoutPlansForAdmin() {
        return ApiResponse.success(
                "Get all workout plans successfully",
                workoutPlanService.getAllWorkoutPlansForAdmin()
        );
    }

    @PutMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WorkoutPlanResponse> updateWorkoutPlanForAdmin(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update workout plan successfully",
                workoutPlanService.updateWorkoutPlan(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteWorkoutPlanForAdmin(
            @PathVariable Long id
    ) {
        workoutPlanService.deleteWorkoutPlan(
                id
        );

        return ApiResponse.success(
                "Delete workout plan successfully"
        );
    }

    // =========================================================
    // AUTH
    // =========================================================

    private String getPrincipal(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || authentication.getName() == null
                        || authentication.getName().isBlank()
                        || "anonymousUser".equals(
                        authentication.getName()
                )
                        || "anonymous".equals(
                        authentication.getName()
                )
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return authentication.getName();
    }
}