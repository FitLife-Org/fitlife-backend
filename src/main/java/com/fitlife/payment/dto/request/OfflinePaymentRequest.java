package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OfflinePaymentRequest {

    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private BigDecimal amount;

    private String note;
}
