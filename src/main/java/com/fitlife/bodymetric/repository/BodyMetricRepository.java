package com.fitlife.bodymetric.repository;

import com.fitlife.bodymetric.entity.BodyMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BodyMetricRepository extends JpaRepository<BodyMetric, Long> {

    Page<BodyMetric> findByMemberIdOrderByRecordedAtDesc(Long memberId, Pageable pageable);

    Optional<BodyMetric> findTopByMemberIdOrderByRecordedAtDesc(Long memberId);

    Optional<BodyMetric> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndMemberId(Long id, Long memberId);

    List<BodyMetric> findByMemberIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long memberId,
            LocalDateTime from,
            LocalDateTime to
    );
}