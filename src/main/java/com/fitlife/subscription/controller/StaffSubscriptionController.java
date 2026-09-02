package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
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
@RequestMapping("/staff")
public class StaffSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/subscriptions")
    public ApiResponse<PageResponse<SubscriptionResponse>> getSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            Pageable pageable
    ) {
        Page<SubscriptionResponse> page =
                subscriptionService.getAllSubscriptions(status, pageable);

        return ApiResponse.<PageResponse<SubscriptionResponse>>builder()
                .data(PageResponse.from(page))
                .build();
    }

    @GetMapping("/subscriptions/{id}")
    public ApiResponse<SubscriptionResponse> getSubscriptionById(
            @PathVariable Long id
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.getSubscriptionByIdForAdmin(id))
                .build();
    }

    @PostMapping("/members/{memberId}/subscriptions")
    public ApiResponse<SubscriptionResponse> createSubscriptionForMemberByStaff(
            @PathVariable Long memberId,
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(
                        subscriptionService.createSubscriptionForMemberByStaff(
                                memberId,
                                request
                        )
                )
                .build();
    }
}