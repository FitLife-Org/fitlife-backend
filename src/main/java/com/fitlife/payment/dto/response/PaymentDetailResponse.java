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
public class PaymentDetailResponse {

    private Long id;

    private String paymentCode;

    private Long invoiceId;

    private String invoiceCode;

    private Long subscriptionId;

    private Long memberId;

    private String memberCode;

    private String memberName;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private String transactionNo;

    private LocalDateTime paidAt;

    private Long confirmedById;

    private String confirmedByName;

    private String note;

    private String failedReason;

    private LocalDateTime cancelledAt;

    private LocalDateTime refundedAt;

    private Long refundedById;

    private String refundedByName;

    private String refundReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}