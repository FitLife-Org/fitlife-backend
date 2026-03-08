package com.fitlife.repository;

import com.fitlife.entity.Member;
import com.fitlife.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Check member have active subscription
    boolean existsByMemberAndStatus(Member member, String status);

    // Find active subscription for member
    Optional<Subscription> findFirstByMemberAndStatus(Member member, String status);

    Optional<Subscription> findFirstByMemberAndStatusOrderByEndDateDesc(Member member, String status);
}