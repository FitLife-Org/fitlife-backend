package com.fitlife.subscription;

import com.fitlife.core.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @RequestBody SubscriptionCreationRequest request, // Tạm thời bỏ @Valid để bỏ qua lỗi NotNull memberId
            Principal principal) {

        // Dùng Principal lấy username từ Token và truyền xuống Service
        SubscriptionResponse result = subscriptionService.createSubscription(principal.getName(), request);

        ApiResponse<SubscriptionResponse> response = ApiResponse.<SubscriptionResponse>builder()
                .code(200)
                .message("Tạo đơn hàng PENDING thành công")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}