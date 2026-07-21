package com.fitlife.workout.controller;

import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;
import com.fitlife.workout.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(request, currentUsername);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyWorkoutPlans() {
        Long memberId = 1L;
        List<WorkoutPlanResponse> responses = workoutPlanService.getMyWorkoutPlans(memberId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyWorkoutPlansMe(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        List<WorkoutPlanResponse> responses = workoutPlanService.getMyWorkoutPlans(currentUsername);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me/active")
    public ResponseEntity<WorkoutPlanDetailResponse> getActiveWorkoutPlan(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.getActiveWorkoutPlan(currentUsername);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutPlanById(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.getWorkoutPlanById(id, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
            @PathVariable("id") Long id,
            @RequestBody WorkoutPlanUpdateRequest request) {

        WorkoutPlanResponse response = workoutPlanService.updateWorkoutPlan(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable("id") Long id) {
        workoutPlanService.deleteWorkoutPlan(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}")
    public ResponseEntity patchWorkoutPlan(
            @PathVariable("id") Long id,
            @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.patchWorkoutPlan(id, request, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/structure")
    public ResponseEntity updateWorkoutPlanStructure(
            @PathVariable("id") Long id,
            @RequestBody List daysRequest,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.updateWorkoutPlanStructure(id, daysRequest, currentUsername);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/activate")
    public ResponseEntity activateWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.activateWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity completeWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.completeWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity archiveWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.archiveWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity cloneWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.cloneWorkoutPlan(id, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}