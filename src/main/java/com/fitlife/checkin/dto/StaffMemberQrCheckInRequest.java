package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffMemberQrCheckInRequest {
    @NotBlank(message = "QR data is required")
    private String qrData;
    private String reason;
}
