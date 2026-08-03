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
public class InvoiceAuditLogResponse {

    private Long id;

    private Long invoiceId;

    private Long actorUserId;

    private String actorName;

    private String actorRoles;

    private InvoiceActionType action;

    private InvoiceStatus oldStatus;

    private InvoiceStatus newStatus;

    private String description;

    private LocalDateTime createdAt;
}