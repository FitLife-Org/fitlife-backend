package com.fitlife.payment.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.payment.dto.PaymentResponse;
import com.fitlife.payment.mapper.PaymentMapper;
import com.fitlife.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Táº¡o link thanh toĂ¡n vĂ  xá»­ lĂ½ callback VNPay")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping("/create-payment")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    @Operation(summary = "Táº¡o link thanh toĂ¡n VNPay", description = "Sinh URL thanh toĂ¡n cho subscription Ä‘Ă£ táº¡o cá»§a há»™i viĂªn.")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Parameter(description = "ID cá»§a subscription cáº§n thanh toĂ¡n", example = "1001")
            @RequestParam("subscriptionId") Long subscriptionId,
            HttpServletRequest request
    ) {
        String paymentUrl = paymentService.createPaymentUrl(subscriptionId, request);
        PaymentResponse paymentResponse = paymentMapper.toResponse(paymentUrl, subscriptionId);

        return ResponseEntity.ok(ApiResponse.success(paymentResponse, "Táº¡o link thanh toĂ¡n thĂ nh cĂ´ng"));
    }

    @GetMapping("/vnpay-return")
    @PreAuthorize("permitAll()")
    @SecurityRequirements()
    @Operation(summary = "VNPay return callback", description = "Endpoint callback public Ä‘Æ°á»£c VNPay redirect vá» sau khi thanh toĂ¡n.")
    public ResponseEntity<ApiResponse<String>> paymentReturn(HttpServletRequest request) {
        String result = paymentService.processPaymentReturn(request);

        if ("SUCCESS".equals(result)) {
            return ResponseEntity.ok(ApiResponse.success(result, "Thanh toĂ¡n thĂ nh cĂ´ng. GĂ³i táº­p Ä‘Ă£ Ä‘Æ°á»£c kĂ­ch hoáº¡t!"));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Giao dá»‹ch tháº¥t báº¡i hoáº·c Ä‘Ă£ bá»‹ há»§y bá».", result));
    }
}