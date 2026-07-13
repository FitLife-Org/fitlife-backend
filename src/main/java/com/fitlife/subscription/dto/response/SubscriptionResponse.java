package com.fitlife.subscription.dto.response;

import com.fitlife.subscription.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SubscriptionResponse {

    private Long id;

    private Long memberId;
    private String memberCode;
    private String memberName;

    private Long gymPackageId;
    private String gymPackageCode;
    private String gymPackageName;

    private Long packageDurationId;
    private String packageDurationCode;
    private String packageDurationName;
    private Integer months;

    private BigDecimal basePrice;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    private Integer ptSessionsTotal;
    private Integer ptSessionsUsed;

    private LocalDate startDate;
    private LocalDate endDate;

    private SubscriptionStatus status;

    private Boolean autoRenew;

    private String note;

    private Long invoiceId;
    private String invoiceCode;
    private BigDecimal invoiceFinalAmount;
    private String invoiceStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}