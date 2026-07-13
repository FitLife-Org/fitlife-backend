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

    Optional<BodyMetric> findByIdAndIsDeletedFalse(Long id);

    Page<BodyMetric> findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long memberId,
            Pageable pageable
    );

    List<BodyMetric> findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long memberId
    );

    Optional<BodyMetric> findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long memberId
    );

    Optional<BodyMetric> findByIdAndMemberIdAndIsDeletedFalse(
            Long id,
            Long memberId
    );

    boolean existsByIdAndMemberIdAndIsDeletedFalse(
            Long id,
            Long memberId
    );

    List<BodyMetric> findByMemberIdAndIsDeletedFalseAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long memberId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
        SELECT b
        FROM BodyMetric b
        JOIN b.member m
        JOIN m.user u
        WHERE b.isDeleted = false
          AND (:memberId IS NULL OR m.id = :memberId)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR u.phone LIKE CONCAT('%', :keyword, '%')
               OR LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:from IS NULL OR b.recordedAt >= :from)
          AND (:to IS NULL OR b.recordedAt <= :to)
        ORDER BY b.recordedAt DESC
    """)
    Page<BodyMetric> searchBodyMetricsByAdmin(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT b
        FROM BodyMetric b
        JOIN FETCH b.member m
        JOIN FETCH m.user
        WHERE b.id = :id
          AND b.isDeleted = false
    """)
    Optional<BodyMetric> findByIdWithMember(@Param("id") Long id);
}