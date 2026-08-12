package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ExpiringSubscriptionItem {
    private Long subscriptionId;
    private String memberName;
    private String memberPhone;
    private String packageName;
    private String packageDurationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysRemaining;
}
