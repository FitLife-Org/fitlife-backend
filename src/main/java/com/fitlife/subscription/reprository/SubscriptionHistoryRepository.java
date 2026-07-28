package com.fitlife.subscription.repository;

import com.fitlife.subscription.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);
}
