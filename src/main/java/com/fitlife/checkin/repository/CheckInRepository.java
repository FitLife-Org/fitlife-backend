package com.fitlife.checkin.repository;

import com.fitlife.checkin.entity.CheckIn;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findFirstByMemberAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Member member);
}

