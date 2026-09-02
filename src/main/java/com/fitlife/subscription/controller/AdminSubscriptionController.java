package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.subscription.dto.request.SubscriptionStatusUpdateRequest;
import com.fitlife.subscription.dto.request.TransferSubscriptionRequest;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subscriptions")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ApiResponse<PageResponse<SubscriptionResponse>> getAllSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            Pageable pageable
    ) {
        Page<SubscriptionResponse> page =
                subscriptionService.getAllSubscriptions(status, pageable);

        return ApiResponse.<PageResponse<SubscriptionResponse>>builder()
                .data(PageResponse.from(page))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getSubscriptionByIdForAdmin(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getSubscriptionByIdForAdmin(id))
                .build();
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<SubscriptionResponse> cancelSubscription(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.cancelSubscription(id))
                .build();
    }

    @PatchMapping("/{id}/expire")
    public ApiResponse<SubscriptionResponse> expireSubscription(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.expireSubscription(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<SubscriptionResponse> updateSubscriptionStatus(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionStatusUpdateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(
                        subscriptionService.updateSubscriptionStatusByAdmin(
                                id,
                                request
                        )
                )
                .build();
    }

    @PostMapping("/{id}/transfer")
    public ApiResponse<SubscriptionResponse> transferSubscription(
            @PathVariable Long id,
            @Valid @RequestBody TransferSubscriptionRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(
                        subscriptionService.transferSubscription(
                                id,
                                request.getRecipientMemberId(),
                                request.getNote()
                        )
                )
                .build();
    }
}