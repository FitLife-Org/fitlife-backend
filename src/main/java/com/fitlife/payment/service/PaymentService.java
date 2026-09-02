package com.fitlife.payment.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.payment.dto.request.PaymentCancelRequest;
import com.fitlife.payment.dto.request.PaymentConfirmRequest;
import com.fitlife.payment.dto.request.PaymentCreateRequest;
import com.fitlife.payment.dto.request.PaymentFailRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PageResponse<PaymentResponse> getMyPayments(Pageable pageable);

    PaymentDetailResponse getMyPaymentById(Long paymentId);

    PageResponse<PaymentResponse> getAllPayments(
            PaymentStatus status,
            PaymentMethod method,
            Long memberId,
            Long invoiceId,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            Pageable pageable
    );

    PaymentDetailResponse getPaymentByIdForAdmin(Long paymentId);

    PaymentDetailResponse confirmPayment(Long paymentId, PaymentConfirmRequest request);

    PaymentDetailResponse failPayment(Long paymentId, PaymentFailRequest request);

    PaymentDetailResponse cancelPayment(Long paymentId, PaymentCancelRequest request);

    PaymentDetailResponse offlinePayment(com.fitlife.payment.dto.request.OfflinePaymentRequest request);
}