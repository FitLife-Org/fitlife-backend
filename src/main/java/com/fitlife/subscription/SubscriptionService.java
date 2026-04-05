package com.fitlife.subscription;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(String username, SubscriptionCreationRequest request);
}
