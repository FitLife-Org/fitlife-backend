package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.WorkoutDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutDetailRepository extends JpaRepository<WorkoutDetail, Long> {

    // Count completed exercises for a given workout plan (for dashboard stats)
    @Query("SELECT COUNT(d) FROM WorkoutDetail d " +
            "JOIN d.session s " +
            "WHERE s.workoutPlan.id = :planId AND d.isCompleted = true")
    int countCompletedExercisesByPlanId(@Param("planId") Long planId);
}