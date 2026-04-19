<<<<<<<< HEAD:src/main/java/com/fitlife/ai/repository/AiWorkoutPlanRepository.java
package com.fitlife.ai.repository;
========
package com.fitlife.ai_workout.repository;
>>>>>>>> origin/main:src/main/java/com/fitlife/ai_workout/repository/AiWorkoutPlanRepository.java

import com.fitlife.ai.entity.AiWorkoutPlan;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiWorkoutPlanRepository extends JpaRepository<AiWorkoutPlan, Long> {
    List<AiWorkoutPlan> findByMemberOrderByCreatedAtDesc(Member member);
}