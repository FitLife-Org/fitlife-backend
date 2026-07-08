package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VnpayCreateUrlRequest {

    @NotNull(message = "Invoice id is required")
    private Long invoiceId;
}