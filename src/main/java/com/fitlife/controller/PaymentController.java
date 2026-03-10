package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.PaymentResponse;
import com.fitlife.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestParam("subscriptionId") Long subscriptionId,
            HttpServletRequest request
    ) {
        // Gọi service tạo URL
        String paymentUrl = paymentService.createPaymentUrl(subscriptionId, request);

        // Chuẩn bị thông tin phản hồi
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .status("OK")
                .message("Tạo link thanh toán thành công")
                .paymentUrl(paymentUrl)
                .orderInfo("Thanh toán Subscription ID: " + subscriptionId)
                .build();

        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Thành công")
                .data(paymentResponse)
                .build());
    }

    @GetMapping("/vnpay-return")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> paymentReturn(HttpServletRequest request) {
        String result = paymentService.processPaymentReturn(request);

        if ("SUCCESS".equals(result)) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(200)
                    .message("Thanh toán thành công. Gói tập đã được kích hoạt!")
                    .data(result)
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<String>builder()
                .code(400)
                .message("Giao dịch thất bại hoặc đã bị hủy bỏ.")
                .data(result)
                .build());
    }
}