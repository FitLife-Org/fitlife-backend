package com.fitlife.payment.service;

import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;

import java.util.Map;

public interface VnpayService {

    VnpayCreateUrlResponse createPaymentUrl(
            VnpayCreateUrlRequest request,
            String ipAddress
    );

    String handleReturn(Map<String, String> params);

    Map<String, String> handleIpn(Map<String, String> params);
}