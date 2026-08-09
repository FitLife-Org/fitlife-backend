package com.fitlife.payment.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.payment.dto.request.PaymentCreateRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        return ApiResponse.<PaymentResponse>builder()
                .data(paymentService.createPayment(request))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<PaymentResponse>> getMyPayments(Pageable pageable) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .data(paymentService.getMyPayments(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentDetailResponse> getMyPaymentById(@PathVariable Long id) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .data(paymentService.getMyPaymentById(id))
                .build();
    }
}