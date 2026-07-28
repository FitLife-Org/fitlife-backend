package com.fitlife.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VnpayCreateUrlRequest {

    private Long invoiceId;
    private Long subscriptionId;
    private String bankCode;
}