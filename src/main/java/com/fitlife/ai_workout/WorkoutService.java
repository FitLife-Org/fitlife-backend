package com.fitlife.ai_workout;

public interface WorkoutService {
    WorkoutPlan getCurrentPlanByUsername(String username);

    void toggleWorkoutDetailStatus(Long detailId);
}
