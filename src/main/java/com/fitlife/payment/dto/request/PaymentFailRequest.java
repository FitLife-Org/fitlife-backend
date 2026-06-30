package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentFailRequest {

    @NotBlank(message = "FAILED_REASON_REQUIRED")
    @Size(max = 500, message = "FAILED_REASON_TOO_LONG")
    private String reason;
}