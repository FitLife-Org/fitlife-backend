package com.fitlife.payment.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;
import com.fitlife.payment.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

        return ApiResponse.success(
                "Tạo URL thanh toán VNPay thành công",
                vnpayService.createPaymentUrl(request, ipAddress)
        );
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}