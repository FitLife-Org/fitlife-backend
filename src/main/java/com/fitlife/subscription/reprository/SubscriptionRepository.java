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

    java.util.List<Subscription> findByMemberIdOrderByIdDesc(Long memberId);

    java.util.List<Subscription> findByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    Optional<Subscription> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId,
            SubscriptionStatus status
    );

    boolean existsByMemberIdAndStatus(
            Long memberId,
            SubscriptionStatus status
    );

    boolean existsByGymPackageIdAndStatus(Long gymPackageId, SubscriptionStatus status);

    boolean existsByPackageDurationIdAndStatus(Long packageDurationId, SubscriptionStatus status);

    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    Page<Subscription> findByGymPackageId(Long gymPackageId, Pageable pageable);

    Page<Subscription> findByEndDateBeforeAndStatus(
            LocalDate date,
            SubscriptionStatus status,
            Pageable pageable
    );

    java.util.List<Subscription> findByStatusAndEndDateBefore(
            SubscriptionStatus status,
            LocalDate date
    );
}