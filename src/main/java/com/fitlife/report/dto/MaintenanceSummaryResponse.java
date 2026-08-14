package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class MaintenanceSummaryResponse {
    private long totalSchedules;
    private long pendingSchedules;
    private long inProgressSchedules; // Map SCHEDULED sang pending, inProgress có thể để 0 hoặc tương đương
    private long completedSchedules;
    private BigDecimal totalMaintenanceCost;
}
