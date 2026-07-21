package com.fitlife.workout.controller;

import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanDayRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
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
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    // WORKOUT-01: POST /workout-plans
    @PostMapping("/workout-plans")
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(request, currentUsername);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/workout-plans/my")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyWorkoutPlans() {
        Long memberId = 1L;
        List<WorkoutPlanResponse> responses = workoutPlanService.getMyWorkoutPlans(memberId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/workout-plans/me")
    public ResponseEntity<List<WorkoutPlanResponse>> getMyWorkoutPlansMe(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        List<WorkoutPlanResponse> responses = workoutPlanService.getMyWorkoutPlans(currentUsername);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/workout-plans/me/active")
    public ResponseEntity<WorkoutPlanDetailResponse> getActiveWorkoutPlan(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.getActiveWorkoutPlan(currentUsername);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/workout-plans/{id}")
    public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutPlanById(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.getWorkoutPlanById(id, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/workout-plans/{id}")
    public ResponseEntity<WorkoutPlanResponse> patchWorkoutPlan(
            @PathVariable("id") Long id,
            @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.patchWorkoutPlan(id, request, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/workout-plans/{id}/structure")
    public ResponseEntity<WorkoutPlanDetailResponse> updateWorkoutPlanStructure(
            @PathVariable("id") Long id,
            @RequestBody List<WorkoutPlanDayRequest> daysRequest,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDetailResponse response = workoutPlanService.updateWorkoutPlanStructure(id, daysRequest, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/workout-plans/{id}/activate")
    public ResponseEntity<WorkoutPlanResponse> activateWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.activateWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/workout-plans/{id}/complete")
    public ResponseEntity<WorkoutPlanResponse> completeWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.completeWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/workout-plans/{id}/archive")
    public ResponseEntity<WorkoutPlanResponse> archiveWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.archiveWorkoutPlan(id, currentUsername);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/workout-plans/{id}/clone")
    public ResponseEntity<WorkoutPlanResponse> cloneWorkoutPlan(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.cloneWorkoutPlan(id, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/workout-plans/me/today")
    public ResponseEntity<WorkoutPlanDayResponse> getTodayWorkoutDay(Authentication authentication) {
        String currentUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanDayResponse response = workoutPlanService.getTodayWorkoutDay(currentUsername);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/trainer/members/{memberId}/workout-plans")
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlanForMember(
            @PathVariable("memberId") Long memberId,
            @RequestBody WorkoutPlanCreateRequest request,
            Authentication authentication) {

        String trainerUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlanForMember(memberId, request, trainerUsername);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trainer/members/{memberId}/workout-plans")
    public ResponseEntity<List<WorkoutPlanResponse>> getMemberWorkoutPlansForTrainer(
            @PathVariable("memberId") Long memberId,
            Authentication authentication) {

        String trainerUsername = (authentication != null) ? authentication.getName() : "anonymous";
        List<WorkoutPlanResponse> responses = workoutPlanService.getMemberWorkoutPlansForTrainer(memberId, trainerUsername);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/workout-plans/{id}")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
            @PathVariable("id") Long id,
            @RequestBody WorkoutPlanUpdateRequest request) {

        WorkoutPlanResponse response = workoutPlanService.updateWorkoutPlan(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/workout-plans/{id}")
    public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable("id") Long id) {
        workoutPlanService.deleteWorkoutPlan(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/trainer/members/{memberId}/workout-plans/{id}")
    public ResponseEntity patchWorkoutPlanForMember(
            @PathVariable("memberId") Long memberId,
            @PathVariable("id") Long id,
            @RequestBody WorkoutPlanUpdateRequest request,
            Authentication authentication) {

        String trainerUsername = (authentication != null) ? authentication.getName() : "anonymous";
        WorkoutPlanResponse response = workoutPlanService.patchWorkoutPlanForMember(memberId, id, request, trainerUsername);
        return ResponseEntity.ok(response);
    }
}