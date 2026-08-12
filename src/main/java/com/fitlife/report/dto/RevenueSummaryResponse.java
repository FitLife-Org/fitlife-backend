package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class RevenueSummaryResponse {
    private BigDecimal totalRevenue;
    private BigDecimal cashRevenue;
    private BigDecimal bankTransferRevenue;
    private BigDecimal vnpayRevenue;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
}
