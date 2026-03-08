package com.fitlife.repository;

import com.fitlife.entity.HealthMetric;
import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    // Find all health metrics for a member, ordered by recorded date descending
    List<HealthMetric> findByMemberIdOrderByRecordedDateDesc(Long memberId);

    Optional<HealthMetric> findFirstByMemberOrderByRecordedDateDesc(Member member);

}
