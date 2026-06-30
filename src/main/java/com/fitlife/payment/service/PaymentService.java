package com.fitlife.payment.service;

import com.fitlife.common.dto.PageResponse;
import com.fitlife.payment.dto.request.PaymentCancelRequest;
import com.fitlife.payment.dto.request.PaymentConfirmRequest;
import com.fitlife.payment.dto.request.PaymentCreateRequest;
import com.fitlife.payment.dto.request.PaymentFailRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PageResponse<PaymentResponse> getMyPayments(Pageable pageable);

    PaymentDetailResponse getMyPaymentById(Long paymentId);

    PageResponse<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable);

    PaymentDetailResponse getPaymentByIdForAdmin(Long paymentId);

    PaymentDetailResponse confirmPayment(Long paymentId, PaymentConfirmRequest request);

    PaymentDetailResponse failPayment(Long paymentId, PaymentFailRequest request);

    PaymentDetailResponse cancelPayment(Long paymentId, PaymentCancelRequest request);
}