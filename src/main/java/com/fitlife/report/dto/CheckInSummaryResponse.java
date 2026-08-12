package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckInSummaryResponse {
    private long todayCheckIns;
    private long yesterdayCheckIns;
    private double dailyGrowthRate;
    private long thisMonthCheckIns;
    private long lastMonthCheckIns;
    private double monthlyGrowthRate;
    private long averageDailyCheckInsThisMonth;
}
