package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SubscriptionSummaryResponse {
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long pendingSubscriptions;
    private long expiredSubscriptions;
    private long cancelledSubscriptions;
    private List<PackageDistributionItem> packageDistribution;
}
