package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import com.fitlife.payment.enums.PaymentMethod;

import java.math.BigDecimal;

@Getter
@Setter
public class OfflinePaymentRequest {

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    private String note;
}
