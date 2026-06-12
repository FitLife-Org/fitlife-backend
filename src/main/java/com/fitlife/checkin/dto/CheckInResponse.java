package com.fitlife.checkin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(name = "CheckInResponse", description = "Káº¿t quáº£ check-in cá»§a há»™i viĂªn")
public class CheckInResponse {
    @Schema(description = "ID há»™i viĂªn", example = "100")
    private Long memberId;
    @Schema(description = "TĂªn há»™i viĂªn", example = "Nguyen Van A")
    private String memberName;
    @Schema(description = "Thá»i gian check-in", example = "2026-04-27T08:30:00")
    private LocalDateTime checkInTime;
    @Schema(description = "Tráº¡ng thĂ¡i truy cáº­p", example = "ACCESS_GRANTED")
    private String status; // "ACCESS_GRANTED" hoáº·c "ACCESS_DENIED"
    @Schema(description = "ThĂ´ng Ä‘iá»‡p chi tiáº¿t", example = "Check-in thĂ nh cĂ´ng")
    private String message; // Reason details
}