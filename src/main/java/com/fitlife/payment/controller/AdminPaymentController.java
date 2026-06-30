package com.fitlife.payment.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import com.fitlife.payment.dto.request.PaymentCancelRequest;
import com.fitlife.payment.dto.request.PaymentConfirmRequest;
import com.fitlife.payment.dto.request.PaymentFailRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ApiResponse<PageResponse<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long invoiceId,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .data(paymentService.getAllPayments(
                        status,
                        method,
                        memberId,
                        invoiceId,
                        pageable
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentDetailResponse> getPaymentByIdForAdmin(@PathVariable Long id) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .data(paymentService.getPaymentByIdForAdmin(id))
                .build();
    }

    @PatchMapping("/{id}/confirm")
    public ApiResponse<PaymentDetailResponse> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .data(paymentService.confirmPayment(id, request))
                .build();
    }

    @PatchMapping("/{id}/fail")
    public ApiResponse<PaymentDetailResponse> failPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentFailRequest request
    ) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .data(paymentService.failPayment(id, request))
                .build();
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<PaymentDetailResponse> cancelPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .data(paymentService.cancelPayment(id, request))
                .build();
    }
}