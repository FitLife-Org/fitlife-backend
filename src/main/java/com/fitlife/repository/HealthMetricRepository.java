package com.fitlife.repository;

import com.fitlife.entity.HealthMetric;
import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {

    /**
     * Lấy chỉ số sức khỏe mới nhất của một hội viên để AI phân tích.
     * Tên hàm phải kết thúc bằng RecordedAtDesc để khớp với biến recordedAt trong Entity.
     */
    Optional<HealthMetric> findFirstByMemberOrderByRecordedAtDesc(Member member);

    /**
     * Lấy danh sách lịch sử chỉ số sức khỏe của hội viên.
     * (Đây là hàm gây lỗi trong log của em, anh đã sửa Date -> At).
     */
    List<HealthMetric> findByMemberIdOrderByRecordedAtDesc(Long memberId);
}