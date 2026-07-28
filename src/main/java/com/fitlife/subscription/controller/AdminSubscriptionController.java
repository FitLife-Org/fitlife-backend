package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subscriptions")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ApiResponse<?> getAllSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            Pageable pageable
    ) {
        return ApiResponse.builder()
                .data(subscriptionService.getAllSubscriptions(status, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getSubscriptionByIdForAdmin(@PathVariable Long id) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getSubscriptionByIdForAdmin(id))
                .build();
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long id) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.cancelSubscription(id))
                .build();
    }

    @PatchMapping("/{id}/expire")
    public ApiResponse<SubscriptionResponse> expireSubscription(@PathVariable Long id) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.expireSubscription(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<SubscriptionResponse> updateSubscriptionStatus(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.fitlife.subscription.dto.request.SubscriptionStatusUpdateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.updateSubscriptionStatusByAdmin(id, request))
                .build();
    }
}