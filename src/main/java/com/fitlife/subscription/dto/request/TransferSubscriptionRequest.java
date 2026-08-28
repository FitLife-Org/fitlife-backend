package com.fitlife.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferSubscriptionRequest {

    @NotNull(message = "Recipient member ID is required")
    private Long recipientMemberId;

    private String note;
}
