package com.fitlife.subscription.dto.request;

import com.fitlife.subscription.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SubscriptionStatus status;

    private String reason;
}
