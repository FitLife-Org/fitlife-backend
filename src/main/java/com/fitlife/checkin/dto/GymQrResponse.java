package com.fitlife.checkin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymQrResponse {
    private String qrCodeData;
    private LocalDateTime createdAt;
}
