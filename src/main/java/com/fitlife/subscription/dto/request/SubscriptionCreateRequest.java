package com.fitlife.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCreateRequest {

    @NotNull(message = "GYM_PACKAGE_ID_REQUIRED")
    private Long gymPackageId;

    @NotNull(message = "PACKAGE_DURATION_ID_REQUIRED")
    private Long packageDurationId;

    private Boolean autoRenew;

    private String note;
}