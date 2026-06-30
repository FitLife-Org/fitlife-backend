package com.fitlife.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCreateRequest {

    @NotNull(message = "GYM_PACKAGE_ID_REQUIRED")
    private Long gymPackageId;

    private Boolean autoRenew;

    private String note;
}