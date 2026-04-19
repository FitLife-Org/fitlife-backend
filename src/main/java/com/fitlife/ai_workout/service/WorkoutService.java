<<<<<<<< HEAD:src/main/java/com/fitlife/workout/service/WorkoutService.java
package com.fitlife.workout.service;
========
package com.fitlife.ai_workout.service;
>>>>>>>> origin/main:src/main/java/com/fitlife/ai_workout/service/WorkoutService.java

import com.fitlife.workout.entity.WorkoutPlan;

public interface WorkoutService {
    WorkoutPlan getCurrentPlanByUsername(String username);

    void toggleWorkoutDetailStatus(Long detailId);
}
