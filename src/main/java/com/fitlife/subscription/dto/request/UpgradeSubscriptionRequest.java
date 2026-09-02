package com.fitlife.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpgradeSubscriptionRequest {

    @NotNull(message = "New package duration ID is required")
    private Long newPackageDurationId;
}
