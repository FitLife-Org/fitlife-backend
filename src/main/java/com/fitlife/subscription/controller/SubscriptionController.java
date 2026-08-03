package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.response.SubscriptionPreviewResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/preview-price")
    public ApiResponse<SubscriptionPreviewResponse> previewPrice(
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ApiResponse.<SubscriptionPreviewResponse>builder()
                .data(subscriptionService.previewPrice(request))
                .build();
    }

    @PostMapping
    public ApiResponse<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.createSubscription(request))
                .build();
    }

    @GetMapping({"/my", "/me"})
    public ApiResponse<?> getMySubscriptions(Pageable pageable) {
        return ApiResponse.builder()
                .data(subscriptionService.getMySubscriptions(pageable))
                .build();
    }

    @GetMapping({"/my/active", "/me/active"})
    public ApiResponse<SubscriptionResponse> getMyActiveSubscription() {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getMyActiveSubscription())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getMySubscriptionById(@PathVariable Long id) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getMySubscriptionById(id))
                .build();
    }

    @PostMapping("/{id}/renew")
    public ApiResponse<SubscriptionResponse> renewSubscription(
            @PathVariable Long id,
            @RequestBody(required = false) SubscriptionCreateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.renewSubscription(id))
                .build();
    }

    @PostMapping("/{id}/upgrade")
    public ApiResponse<SubscriptionResponse> upgradeSubscription(
            @PathVariable Long id,
            @Valid @RequestBody com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.upgradeSubscription(id, request))
                .build();
    }

    @PostMapping("/{id}/change-package")
    public ApiResponse<SubscriptionResponse> changePackageSameTier(
            @PathVariable Long id,
            @Valid @RequestBody com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.changePackageSameTier(id, request))
                .build();
    }
}