package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class RevenueTrendItem {
    private String period;
    private BigDecimal revenue;
    private long transactionCount;
}
