package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffSubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/members/{memberId}/subscriptions")
    public ApiResponse<SubscriptionResponse> createSubscriptionForMemberByStaff(
            @PathVariable Long memberId,
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ApiResponse.<SubscriptionResponse>builder()
                .data(subscriptionService.createSubscriptionForMemberByStaff(memberId, request))
                .build();
    }
}
