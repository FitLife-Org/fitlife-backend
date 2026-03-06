package com.fitlife.repository;

import com.fitlife.entity.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    // Truy xuất lịch sử sức khỏe của hội viên, sắp xếp ngày mới nhất lên đầu
    List<HealthMetric> findByMemberIdOrderByRecordedDateDesc(Long memberId);
}
