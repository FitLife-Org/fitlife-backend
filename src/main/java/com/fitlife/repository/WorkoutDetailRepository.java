package com.fitlife.repository;

import com.fitlife.entity.WorkoutDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutDetailRepository extends JpaRepository<WorkoutDetail, Long> {
}