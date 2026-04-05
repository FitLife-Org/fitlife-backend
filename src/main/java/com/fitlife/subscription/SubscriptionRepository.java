package com.fitlife.subscription;

import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Check member have active subscription
    boolean existsByMemberAndStatus(Member member, String status);

    // Find active subscription for member
    Optional<Subscription> findFirstByMemberAndStatus(Member member, String status);

    Optional<Subscription> findFirstByMemberAndStatusOrderByEndDateDesc(Member member, String status);

    List<Subscription> findByStatusAndEndDateBefore(String status, LocalDate currentDate);
}