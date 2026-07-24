package com.fitlife.workout.controller;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
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
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(
                request,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/workout-plans/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyWorkoutPlans(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.getMyWorkoutPlans(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/me/active")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanDetailResponse> getActiveWorkoutPlan(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.getActiveWorkoutPlan(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/me/today")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanDayResponse> getTodayWorkoutDay(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.getTodayWorkoutDay(
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutPlanById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.getWorkoutPlanById(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PatchMapping("/workout-plans/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> patchWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.patchWorkoutPlan(
                        id,
                        request,
                        getPrincipal(authentication)
                )
        );
    }

    @PutMapping("/workout-plans/{id}/structure")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanDetailResponse> updateWorkoutPlanStructure(
            @PathVariable Long id,
            @Valid @RequestBody List<WorkoutPlanDayRequest> daysRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.updateWorkoutPlanStructure(
                        id,
                        daysRequest,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/activate")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> activateWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.activateWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/complete")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> completeWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.completeWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> archiveWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.archiveWorkoutPlan(
                        id,
                        getPrincipal(authentication)
                )
        );
    }

    @PostMapping("/workout-plans/{id}/clone")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> cloneWorkoutPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.cloneWorkoutPlan(
                id,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlanForMember(
            @PathVariable Long memberId,
            @Valid @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication
    ) {
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlanForMember(
                memberId,
                request,
                getPrincipal(authentication)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trainer/members/{memberId}/workout-plans")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<List<WorkoutPlanResponse>> getMemberWorkoutPlansForTrainer(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.getMemberWorkoutPlansForTrainer(
                        memberId,
                        getPrincipal(authentication)
                )
        );
    }

    @PatchMapping("/trainer/members/{memberId}/workout-plans/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<WorkoutPlanResponse> patchWorkoutPlanForMember(
            @PathVariable Long memberId,
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                workoutPlanService.patchWorkoutPlanForMember(
                        memberId,
                        id,
                        request,
                        getPrincipal(authentication)
                )
        );
    }

    @GetMapping("/admin/workout-plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutPlanResponse>> getAllWorkoutPlansForAdmin() {
        return ResponseEntity.ok(
                workoutPlanService.getAllWorkoutPlansForAdmin()
        );
    }

    @PutMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlanForAdmin(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(
                workoutPlanService.updateWorkoutPlan(id, request)
        );
    }

    @DeleteMapping("/admin/workout-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkoutPlanForAdmin(
            @PathVariable Long id
    ) {
        workoutPlanService.deleteWorkoutPlan(id);
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

