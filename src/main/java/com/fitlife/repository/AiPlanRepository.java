package com.fitlife.repository;

import com.fitlife.entity.AiWorkoutPlan;
import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiPlanRepository extends JpaRepository<AiWorkoutPlan, Long> {
    List<AiWorkoutPlan> findByMemberOrderByCreatedAtDesc(Member member);
}