package com.fitlife.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(name = "SubscriptionResponse", description = "ThĂ´ng tin subscription tráº£ vá» tá»« há»‡ thá»‘ng")
public class SubscriptionResponse {
    @Schema(description = "ID subscription", example = "5001")
    private Long id;
    @Schema(description = "ID há»™i viĂªn", example = "100")
    private Long memberId;
    @Schema(description = "ID gĂ³i táº­p", example = "1")
    private Long packageId;
    @Schema(description = "TĂªn gĂ³i táº­p", example = "Premium 12 Months")
    private String packageName;
    @Schema(description = "NgĂ y báº¯t Ä‘áº§u", example = "2026-04-27")
    private LocalDate startDate;
    @Schema(description = "NgĂ y káº¿t thĂºc", example = "2027-04-26")
    private LocalDate endDate;
    @Schema(description = "Tráº¡ng thĂ¡i subscription", example = "PENDING")
    private String status;
}