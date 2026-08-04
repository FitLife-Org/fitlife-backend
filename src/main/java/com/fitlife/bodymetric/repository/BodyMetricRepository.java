package com.fitlife.bodymetric.repository;

import com.fitlife.bodymetric.entity.BodyMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BodyMetricRepository
        extends JpaRepository<BodyMetric, Long> {

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "createdBy"
            }
    )
    Optional<BodyMetric>
    findByIdAndIsDeletedFalse(
            Long id
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "createdBy"
            }
    )
    Page<BodyMetric>
    findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long memberId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "createdBy"
            }
    )
    Optional<BodyMetric>
    findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "createdBy"
            }
    )
    Optional<BodyMetric>
    findByIdAndMemberIdAndIsDeletedFalse(
            Long id,
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "createdBy"
            }
    )
    List<BodyMetric>
    findByMemberIdAndIsDeletedFalseAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long memberId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query(
            value = """
                    SELECT b
                    FROM BodyMetric b
                    JOIN FETCH b.member m
                    JOIN FETCH m.user u
                    LEFT JOIN FETCH b.createdBy
                    WHERE b.isDeleted = false
                      AND (:memberId IS NULL OR m.id = :memberId)
                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(u.fullName)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(u.email)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR u.phone
                                LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(m.memberCode)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                      AND (:from IS NULL OR b.recordedAt >= :from)
                      AND (:to IS NULL OR b.recordedAt <= :to)
                    """,
            countQuery = """
                    SELECT COUNT(b)
                    FROM BodyMetric b
                    JOIN b.member m
                    JOIN m.user u
                    WHERE b.isDeleted = false
                      AND (:memberId IS NULL OR m.id = :memberId)
                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(u.fullName)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(u.email)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR u.phone
                                LIKE CONCAT('%', :keyword, '%')
                            OR LOWER(m.memberCode)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                      AND (:from IS NULL OR b.recordedAt >= :from)
                      AND (:to IS NULL OR b.recordedAt <= :to)
                    """
    )
    Page<BodyMetric> searchBodyMetricsByAdmin(
            @Param("memberId")
            Long memberId,

            @Param("keyword")
            String keyword,

            @Param("from")
            LocalDateTime from,

            @Param("to")
            LocalDateTime to,

            Pageable pageable
    );
}