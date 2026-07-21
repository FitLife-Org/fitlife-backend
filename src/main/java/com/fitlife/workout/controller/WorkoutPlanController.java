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

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutPlanById(@PathVariable("id") Long id) {
        WorkoutPlanDetailResponse response = workoutPlanService.getWorkoutPlanById(id);
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
    @GetMapping("/me/active")
    public ResponseEntity getActiveWorkoutPlan(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.getActiveWorkoutPlan(currentUsername);
        return ResponseEntity.ok(response);
    }
}