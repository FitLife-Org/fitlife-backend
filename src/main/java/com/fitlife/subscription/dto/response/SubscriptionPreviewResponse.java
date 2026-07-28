package com.fitlife.subscription.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPreviewResponse {

    private String packageName;
    private String durationName;
    private BigDecimal basePrice;
    private Integer months;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private Integer ptSessionsTotal;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
}
