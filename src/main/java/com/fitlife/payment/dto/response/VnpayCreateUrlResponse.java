package com.fitlife.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VnpayCreateUrlResponse {

    private Long paymentId;

    private String paymentCode;

    private String paymentUrl;

    private BigDecimal amount;
}