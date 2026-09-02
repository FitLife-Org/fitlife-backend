package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest;
import com.fitlife.subscription.dto.response.SubscriptionPreviewResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ApiResponse<PageResponse<SubscriptionResponse>> getMySubscriptions(
            Pageable pageable
    ) {
        Page<SubscriptionResponse> page =
                subscriptionService.getMySubscriptions(pageable);

        return ApiResponse.<PageResponse<SubscriptionResponse>>builder()
                .data(PageResponse.from(page))
                .build();
    }

    @GetMapping({"/my/active", "/me/active"})
    public ApiResponse<SubscriptionResponse> getMyActiveSubscription() {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getMyActiveSubscription())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getMySubscriptionById(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getMySubscriptionById(id))
                .build();
    }

    @PostMapping("/{id}/renew")
    public ApiResponse<SubscriptionResponse> renewSubscription(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.renewSubscription(id))
                .build();
    }

    @PostMapping("/{id}/upgrade")
    public ApiResponse<SubscriptionResponse> upgradeSubscription(
            @PathVariable Long id,
            @Valid @RequestBody UpgradeSubscriptionRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.upgradeSubscription(id, request))
                .build();
    }

    @PostMapping("/{id}/change-package")
    public ApiResponse<SubscriptionResponse> changePackageSameTier(
            @PathVariable Long id,
            @Valid @RequestBody UpgradeSubscriptionRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.changePackageSameTier(id, request))
                .build();
    }
}