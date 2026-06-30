package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequest {

    @Size(max = 100, message = "TRANSACTION_NO_TOO_LONG")
    private String transactionNo;

    @Size(max = 500, message = "PAYMENT_NOTE_TOO_LONG")
    private String note;
}