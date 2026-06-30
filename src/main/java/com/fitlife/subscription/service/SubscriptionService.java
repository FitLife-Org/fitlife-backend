package com.fitlife.subscription.service;

import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(SubscriptionCreateRequest request);

    Page<SubscriptionResponse> getMySubscriptions(Pageable pageable);

    SubscriptionResponse getMyActiveSubscription();

    SubscriptionResponse getMySubscriptionById(Long subscriptionId);

    Page<SubscriptionResponse> getAllSubscriptions(SubscriptionStatus status, Pageable pageable);

    SubscriptionResponse getSubscriptionByIdForAdmin(Long subscriptionId);

    SubscriptionResponse cancelSubscription(Long subscriptionId);
}