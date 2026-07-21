package com.fitlife.workout.service;

import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;

import java.util.List;

public interface WorkoutPlanService {
    WorkoutPlanResponse createWorkoutPlan(WorkoutPlanCreateRequest request, String currentUsername);


    List getMyWorkoutPlans(Long memberId);
    List getMyWorkoutPlans(String currentUsername);

    WorkoutPlanDetailResponse getActiveWorkoutPlan(String currentUsername);

    WorkoutPlanDetailResponse getWorkoutPlanById(Long id);

    WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanUpdateRequest request);
    void deleteWorkoutPlan(Long id);

    WorkoutPlanDetailResponse getWorkoutPlanById(Long id, String currentUsername);



}