package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StaffDashboardResponse {
    private long todayCheckIns;
    private long expiringSubscriptionsCount;
    private long equipmentNeedingMaintenanceCount;
    private long unpaidInvoicesCount;
}
