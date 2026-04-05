package com.fitlife.attendance;

import com.fitlife.attendance.entity.CheckInHistory;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistory, Long> {

    // Anti-Spam check-in function
    @Query("SELECT c FROM CheckInHistory c WHERE c.member = :member " +
            "AND c.checkInTime >= :startOfDay AND c.checkInTime <= :endOfDay " +
            "AND c.status = 'ACCESS_GRANTED'")
    Optional<CheckInHistory> findSuccessfulCheckInToday(
            @Param("member") Member member,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    //  Count check-ins in a given period for dashboard stats
    @Query("SELECT COUNT(c) FROM CheckInHistory c WHERE c.member.id = :memberId " +
            "AND c.checkInTime >= :startDate AND c.checkInTime <= :endDate " +
            "AND c.status = 'ACCESS_GRANTED'")
    int countCheckinsInPeriod(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM CheckInHistory c WHERE c.checkInTime >= CURRENT_DATE AND c.status = 'ACCESS_GRANTED'")
    long countCheckinsToday();


}