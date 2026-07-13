package com.fitlife.checkin.repository;

import com.fitlife.checkin.entity.CheckIn;
import com.fitlife.checkin.enums.CheckInMethod;
import com.fitlife.checkin.enums.CheckInStatus;
import com.fitlife.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Page<CheckIn> findByDeletedFalse(Pageable pageable);

    Optional<CheckIn> findByIdAndDeletedFalse(Long id);

    Page<CheckIn> findByMemberIdAndDeletedFalseOrderByCheckInTimeDesc(
            Long memberId,
            Pageable pageable
    );

    @Query("""
        SELECT c FROM CheckIn c
        WHERE c.member.id = :memberId
          AND c.checkInTime >= :fromDate
          AND c.checkInTime <= :toDate
          AND c.deleted = false
    """)
    Page<CheckIn> findMyCheckIns(
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    boolean existsByMemberIdAndCheckInTimeBetweenAndStatusAndDeletedFalse(
            Long memberId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            CheckInStatus status
    );

    long countByCheckInTimeBetweenAndStatusAndDeletedFalse(
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            CheckInStatus status
    );

    long countByCheckInTimeBetweenAndCheckInMethodAndStatusAndDeletedFalse(
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            CheckInMethod method,
            CheckInStatus status
    );

    // Independent query to fetch subscription details without modifying the subscription module
    @Query("""
        SELECT s FROM Subscription s
        WHERE s.member.id = :memberId
          AND s.status = 'ACTIVE'
        ORDER BY s.endDate DESC
    """)
    List<Subscription> findActiveSubscriptionsByMemberId(@Param("memberId") Long memberId);

    // Advanced search query for Admin/Staff check-in list
    @Query("""
        SELECT c FROM CheckIn c
        JOIN c.member m
        JOIN m.user u
        WHERE c.deleted = false
          AND (:memberId IS NULL OR c.member.id = :memberId)
          AND (:status IS NULL OR c.status = :status)
          AND (:fromDate IS NULL OR c.checkInTime >= :fromDate)
          AND (:toDate IS NULL OR c.checkInTime <= :toDate)
          AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR u.phone LIKE CONCAT('%', :keyword, '%')
          )
    """)
    Page<CheckIn> searchCheckIns(
            @Param("keyword") String keyword,
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") CheckInStatus status,
            Pageable pageable
    );
}
