package com.fitlife.repository;

import com.fitlife.entity.CheckInHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistory, Long> {

    // (Optional) Method to find check-in history by member ID, ordered by check-in time descending
    // List<CheckInHistory> findByMemberIdOrderByCheckInTimeDesc(Long memberId);
}