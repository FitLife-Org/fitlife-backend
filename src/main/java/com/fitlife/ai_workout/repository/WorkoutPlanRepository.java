<<<<<<<< HEAD:src/main/java/com/fitlife/workout/repository/WorkoutPlanRepository.java
package com.fitlife.workout.repository;
========
package com.fitlife.ai_workout.repository;
>>>>>>>> origin/main:src/main/java/com/fitlife/ai_workout/repository/WorkoutPlanRepository.java

import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    @EntityGraph(value = "WorkoutPlan.fullGraph", type = EntityGraph.EntityGraphType.LOAD)
    List<WorkoutPlan> findByMemberAndStatus(Member member, String status);
}