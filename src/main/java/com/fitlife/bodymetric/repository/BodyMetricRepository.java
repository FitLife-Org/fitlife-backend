package com.fitlife.bodymetric.repository;

import com.fitlife.bodymetric.entity.BodyMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT b FROM BodyMetric b JOIN b.member m JOIN m.user u WHERE " +
            "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:from IS NULL OR b.recordedAt >= :from) " +
            "AND (:to IS NULL OR b.recordedAt <= :to) " +
            "ORDER BY b.recordedAt DESC")
    Page<BodyMetric> searchBodyMetricsByAdmin(
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
    // Trong BodyMetricRepository.java
    @Query("SELECT b FROM BodyMetric b LEFT JOIN FETCH b.member WHERE b.id = :id")
    Optional<BodyMetric> findByIdWithMember(@Param("id") Long id);
}