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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping("/workout-plans")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(
                request,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", response));
    }

    @GetMapping("/workout-plans/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> getMyWorkoutPlans(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getMyWorkoutPlans(
                        getPrincipal(authentication)
                )
        ));
    }

    @GetMapping("/workout-plans/me/active")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanDetailResponse>> getActiveWorkoutPlan(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getActiveWorkoutPlan(
                        getPrincipal(authentication)
                )
        ));
    }

    @GetMapping("/workout-plans/me/today")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanDayResponse>> getTodayWorkoutDay(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getTodayWorkoutDay(
                        getPrincipal(authentication)
                )
        ));
    }

    @GetMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanDetailResponse>> getWorkoutPlanById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getWorkoutPlanById(
                        id,
                        getPrincipal(authentication)
                )
        ));
    }

    @PatchMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> patchWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.patchWorkoutPlan(
                        id,
                        request,
                        getPrincipal(authentication)
                )
        ));
    }

    @PutMapping("/workout-plans/{id}/structure")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanDetailResponse>> updateWorkoutPlanStructure(
            @PathVariable Long id,
            @Valid @RequestBody List<WorkoutPlanDayRequest> daysRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.updateWorkoutPlanStructure(
                        id,
                        daysRequest,
                        getPrincipal(authentication)
                )
        ));
    }

    @PostMapping("/workout-plans/{id}/activate")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> activateWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.activateWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        ));
    }

    @PostMapping("/workout-plans/{id}/complete")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> completeWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.completeWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        ));
    }

    @PostMapping("/workout-plans/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> archiveWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.archiveWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        ));
    }

    @PostMapping("/workout-plans/{id}/clone")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> cloneWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.cloneWorkoutPlan(
                id,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", response));
    }

    @PostMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> createWorkoutPlanForMember(
            @PathVariable Long memberId,
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlanForMember(
                memberId,
                request,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", response));
    }

    @GetMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> getMemberWorkoutPlansForTrainer(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getMemberWorkoutPlansForTrainer(
                        memberId,
                        getPrincipal(authentication)
                )
        ));
    }

    @PatchMapping("/trainer/members/{memberId}/workout-plans/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> patchWorkoutPlanForMember(
            @PathVariable Long memberId,
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.patchWorkoutPlanForMember(
                        memberId,
                        id,
                        request,
                        getPrincipal(authentication)
                )
        ));
    }

    @GetMapping("/admin/workout-plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> getAllWorkoutPlansForAdmin() {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.getAllWorkoutPlansForAdmin()
        ));
    }

    @PutMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> updateWorkoutPlanForAdmin(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Success", 
                workoutPlanService.updateWorkoutPlan(id, request)
        ));
    }

    @DeleteMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkoutPlanForAdmin(
            @PathVariable Long id
    ) {
        workoutPlanService.deleteWorkoutPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
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

