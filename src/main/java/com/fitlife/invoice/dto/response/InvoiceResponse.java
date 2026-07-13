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

    private Long memberId;

    private Long subscriptionId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private InvoiceStatus status;

    private LocalDateTime issuedAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private String note;
}