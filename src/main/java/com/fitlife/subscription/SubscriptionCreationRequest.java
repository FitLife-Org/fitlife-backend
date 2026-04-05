package com.fitlife.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionCreationRequest {

    @NotNull(message = "ID Hội viên không được để trống")
    private Long memberId;

    @NotNull(message = "ID Gói tập không được để trống")
    private Long packageId;

    private String paymentMethod;
}