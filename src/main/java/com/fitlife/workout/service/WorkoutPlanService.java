package com.fitlife.workout.service;

import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;

import java.util.List;

public interface WorkoutPlanService {
    WorkoutPlanResponse createWorkoutPlan(WorkoutPlanCreateRequest request, String currentUsername);
    List getMyWorkoutPlans(Long memberId);
    List getMyWorkoutPlans(String currentUsername);
    WorkoutPlanDetailResponse getActiveWorkoutPlan(String currentUsername);
    WorkoutPlanDetailResponse getWorkoutPlanById(Long id);
    WorkoutPlanDetailResponse getWorkoutPlanById(Long id, String currentUsername);
    WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanUpdateRequest request);

    WorkoutPlanResponse patchWorkoutPlan(Long id, WorkoutPlanUpdateRequest request, String currentUsername);
    WorkoutPlanDetailResponse updateWorkoutPlanStructure(Long id, List daysRequest, String currentUsername);
    WorkoutPlanResponse activateWorkoutPlan(Long id, String currentUsername);
    WorkoutPlanResponse completeWorkoutPlan(Long id, String currentUsername);
    WorkoutPlanResponse archiveWorkoutPlan(Long id, String currentUsername);
    WorkoutPlanResponse cloneWorkoutPlan(Long id, String currentUsername);
    WorkoutPlanDayResponse getTodayWorkoutDay(String currentUsername);
    WorkoutPlanResponse createWorkoutPlanForMember(Long memberId, WorkoutPlanCreateRequest request, String trainerUsername);
    List getMemberWorkoutPlansForTrainer(Long memberId, String trainerUsername);
    WorkoutPlanResponse patchWorkoutPlanForMember(Long memberId, Long id, WorkoutPlanUpdateRequest request, String trainerUsername);
    List getAllWorkoutPlansForAdmin();

    void deleteWorkoutPlan(Long id);
}