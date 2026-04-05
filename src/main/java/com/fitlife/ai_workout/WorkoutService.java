package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.WorkoutPlan;

public interface WorkoutService {
    WorkoutPlan getCurrentPlanByUsername(String username);

    void toggleWorkoutDetailStatus(Long detailId);
}
