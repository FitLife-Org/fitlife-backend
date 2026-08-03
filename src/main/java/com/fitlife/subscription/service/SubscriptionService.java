package com.fitlife.subscription.service;

import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.response.SubscriptionPreviewResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(SubscriptionCreateRequest request);

    SubscriptionPreviewResponse previewPrice(SubscriptionCreateRequest request);

    Page<SubscriptionResponse> getMySubscriptions(Pageable pageable);

    SubscriptionResponse getMyActiveSubscription();

    SubscriptionResponse getMySubscriptionById(Long subscriptionId);

    Page<SubscriptionResponse> getAllSubscriptions(SubscriptionStatus status, Pageable pageable);

    SubscriptionResponse getSubscriptionByIdForAdmin(Long subscriptionId);

    SubscriptionResponse cancelSubscription(Long subscriptionId);

    SubscriptionResponse expireSubscription(Long subscriptionId);

    SubscriptionResponse renewSubscription(Long subscriptionId);

    SubscriptionResponse upgradeSubscription(Long subscriptionId, com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest request);

    SubscriptionResponse changePackageSameTier(Long subscriptionId, com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest request);

    SubscriptionResponse createSubscriptionForMemberByStaff(Long memberId, SubscriptionCreateRequest request);

    SubscriptionResponse updateSubscriptionStatusByAdmin(Long subscriptionId, com.fitlife.subscription.dto.request.SubscriptionStatusUpdateRequest request);

    void activateSubscriptionAfterPayment(Long subscriptionId);
}