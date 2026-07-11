package com.fitlife.payment.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;
import com.fitlife.payment.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VnpayController {

    private final VnpayService vnpayService;

    @PostMapping("/payments/vnpay/create-url")
    public ApiResponse<VnpayCreateUrlResponse> createVnpayPaymentUrl(
            @Valid @RequestBody VnpayCreateUrlRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = getIpAddress(servletRequest);

        return ApiResponse.<VnpayCreateUrlResponse>builder()
                .code(200)
                .message("Tạo URL thanh toán VNPay thành công")
                .data(vnpayService.createPaymentUrl(request, ipAddress))
                .build();
    }

    @GetMapping("/payments/vnpay/return")
    public void vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl = vnpayService.handleReturn(params);
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/payments/vnpay/ipn")
    public Map<String, String> vnpayIpn(
            @RequestParam Map<String, String> params
    ) {
        return vnpayService.handleIpn(params);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}