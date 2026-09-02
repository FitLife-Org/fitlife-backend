package com.fitlife.invoice.dto.response;

import com.fitlife.invoice.enums.InvoiceActionType;
import com.fitlife.invoice.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InvoiceHistoryResponse {

    private Long id;

    private Long invoiceId;

    private InvoiceStatus oldStatus;

    private InvoiceStatus newStatus;

    private InvoiceActionType action;

    private Long changedById;

    private String changedByName;

    private String notes;

    private LocalDateTime createdAt;
}