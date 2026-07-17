package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfCheckInRequest {
    @NotBlank(message = "Gym QR code data is required")
    private String gymQrData;
}
