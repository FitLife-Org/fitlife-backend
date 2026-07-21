package com.fitlife.workout.service;

import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;

import java.util.List;

public interface WorkoutPlanService {
    WorkoutPlanResponse createWorkoutPlan(WorkoutPlanCreateRequest request, String currentUsername);
    List getMyWorkoutPlans(Long memberId);
    WorkoutPlanDetailResponse getWorkoutPlanById(Long id);
    WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanUpdateRequest request);
    void deleteWorkoutPlan(Long id);
}