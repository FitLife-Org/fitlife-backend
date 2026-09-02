package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalRevenueThisMonth;
    private double revenueGrowthRate;
    private long activeMembersCount;
    private long newMembersThisMonth;
    private long todayCheckInsCount;
    private long activeSubscriptionsCount;
    private long equipmentNeedingMaintenanceCount;
}
