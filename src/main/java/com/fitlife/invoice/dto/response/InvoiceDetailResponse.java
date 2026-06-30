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
public class InvoiceDetailResponse {

    private Long id;
    private String invoiceCode;

    private Long memberId;
    private String memberCode;
    private String memberName;

    private Long subscriptionId;
    private String packageName;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private InvoiceStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    private String cancelReason;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}