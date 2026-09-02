package com.fitlife.invoice.dto.response;

import com.fitlife.invoice.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InvoiceResponse {

    private Long id;

    private String invoiceCode;

    // Member
    private Long memberId;
    private String memberCode;
    private String memberName;
    private String memberEmail;
    private String memberPhone;

    // Subscription
    private Long subscriptionId;
    private String packageName;
    private String packageDurationName;

    // Amount
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    // Status
    private InvoiceStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    private String cancelReason;
    private String refundReason;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}