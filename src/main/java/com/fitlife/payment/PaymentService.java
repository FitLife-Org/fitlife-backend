package com.fitlife.payment;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createPaymentUrl(Long subscriptionId, HttpServletRequest request);
    String processPaymentReturn(HttpServletRequest request);
}