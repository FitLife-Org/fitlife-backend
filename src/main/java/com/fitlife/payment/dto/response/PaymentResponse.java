package com.fitlife.payment.dto.response;

import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentResponse {

    private Long id;

    private String paymentCode;

    private Long invoiceId;

    private String invoiceCode;

    private Long subscriptionId;

    private Long memberId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private String transactionNo;

    private LocalDateTime paidAt;

    private String note;

    private LocalDateTime refundedAt;

    private Long refundedById;

    private String refundedByName;

    private String refundReason;

    private LocalDateTime createdAt;
}