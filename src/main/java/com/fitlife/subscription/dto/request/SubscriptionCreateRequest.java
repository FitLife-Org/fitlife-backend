package com.fitlife.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCreateRequest {

    private Long gymPackageId;

    @NotNull(message = "PACKAGE_DURATION_ID_REQUIRED")
    private Long packageDurationId;

    private java.time.LocalDate startDate;

    private String promoCode;

    private Boolean paidCash;

    private Boolean autoRenew;

    private String note;
}