package com.fitlife.invoice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceGenerateRequest {

    @NotNull(message = "SUBSCRIPTION_ID_REQUIRED")
    private Long subscriptionId;

    @Size(max = 500, message = "INVOICE_NOTE_TOO_LONG")
    private String note;
}