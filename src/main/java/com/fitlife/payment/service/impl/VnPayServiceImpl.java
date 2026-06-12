package com.fitlife.payment.service.impl;

import com.fitlife.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class VnPayServiceImpl implements PaymentService {

    @Override
    public String createPaymentUrl(Long subscriptionId, HttpServletRequest request) {
        throw new UnsupportedOperationException("Payment flow must be rebuilt around invoices/payments standard schema");
    }

    @Override
    public String processPaymentReturn(HttpServletRequest request) {
        return "UNSUPPORTED";
    }
}