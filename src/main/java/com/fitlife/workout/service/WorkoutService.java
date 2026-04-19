package com.fitlife.workout.service;

import com.fitlife.workout.entity.WorkoutPlan;

public interface WorkoutService {
    WorkoutPlan getCurrentPlanByUsername(String username);

    void toggleWorkoutDetailStatus(Long detailId);
}
