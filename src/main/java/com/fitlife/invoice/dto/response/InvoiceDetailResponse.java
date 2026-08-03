package com.fitlife.invoice.dto.response;

import com.fitlife.invoice.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InvoiceDetailResponse {

    private Long id;

    private String invoiceCode;

    // =====================================================
    // MEMBER
    // =====================================================

    private Long memberId;

    private String memberCode;

    private String memberName;

    private String memberEmail;

    private String memberPhone;

    // =====================================================
    // SUBSCRIPTION
    // =====================================================

    private Long subscriptionId;

    private String packageName;

    private String packageDurationName;

    private LocalDate subscriptionStartDate;

    private LocalDate subscriptionEndDate;

    // =====================================================
    // AMOUNT
    // =====================================================

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    // =====================================================
    // STATUS
    // =====================================================

    private InvoiceStatus status;

    private LocalDateTime issuedAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    private LocalDateTime refundedAt;

    private Long refundedById;

    private String refundedByName;

    private String refundReason;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}