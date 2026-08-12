package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PaymentStatusDistribution {
    private String status;
    private long count;
    private BigDecimal totalAmount;
}
