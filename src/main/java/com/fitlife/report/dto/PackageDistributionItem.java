package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PackageDistributionItem {
    private Long packageId;
    private String packageName;
    private long count;
    private BigDecimal revenue;
}
