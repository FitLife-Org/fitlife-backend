package com.fitlife.payment.dto.request;

import com.fitlife.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCreateRequest {

    @NotNull(message = "INVOICE_ID_REQUIRED")
    private Long invoiceId;

    @NotNull(message = "PAYMENT_METHOD_REQUIRED")
    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "PAYMENT_NOTE_TOO_LONG")
    private String note;
}