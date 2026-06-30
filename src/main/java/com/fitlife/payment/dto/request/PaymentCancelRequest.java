package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCancelRequest {

    @NotBlank(message = "CANCEL_REASON_REQUIRED")
    @Size(max = 500, message = "CANCEL_REASON_TOO_LONG")
    private String reason;
}