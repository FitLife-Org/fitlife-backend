package com.fitlife.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "SubscriptionCreationRequest", description = "Payload táº¡o subscription cho há»™i viĂªn")
public class SubscriptionCreationRequest {

    @Schema(description = "ID há»™i viĂªn", example = "100")
    @NotNull(message = "ID Há»™i viĂªn khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long memberId;

    @Schema(description = "ID gĂ³i táº­p", example = "1")
    @NotNull(message = "ID GĂ³i táº­p khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long packageId;

    @Schema(description = "PhÆ°Æ¡ng thá»©c thanh toĂ¡n", example = "VNPAY")
    private String paymentMethod;
}