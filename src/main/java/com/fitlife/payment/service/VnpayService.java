package com.fitlife.payment.service;

import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;

public interface VnpayService {

    VnpayCreateUrlResponse createPaymentUrl(
            VnpayCreateUrlRequest request,
            String ipAddress
    );
}