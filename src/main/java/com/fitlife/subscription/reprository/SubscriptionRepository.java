package com.fitlife.subscription.repository;

import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByMemberId(Long memberId, Pageable pageable);

    Optional<Subscription> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId,
            SubscriptionStatus status
    );

    boolean existsByMemberIdAndStatus(
            Long memberId,
            SubscriptionStatus status
    );

    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    Page<Subscription> findByGymPackageId(Long gymPackageId, Pageable pageable);

    Page<Subscription> findByEndDateBeforeAndStatus(
            LocalDate date,
            SubscriptionStatus status,
            Pageable pageable
    );
}