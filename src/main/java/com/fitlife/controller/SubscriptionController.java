package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.SubscriptionCreationRequest;
import com.fitlife.dto.SubscriptionResponse;
import com.fitlife.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MemberController memberController;
    private final UserController userController;

    @PostMapping
    @Transactional(rollbackFor = Exception.class) // Đảm bảo rollback nếu có lỗi xảy ra trong quá trình tạo subscription
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody SubscriptionCreationRequest request) {

        SubscriptionResponse result = subscriptionService.createSubscription(request);

        ApiResponse<SubscriptionResponse> response = ApiResponse.<SubscriptionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Subscription registered successfully")
                .data(result)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}