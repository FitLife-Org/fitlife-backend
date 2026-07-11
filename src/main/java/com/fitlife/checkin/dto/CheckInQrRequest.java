package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInQrRequest {
    @NotBlank(message = "QR data is required")
    private String qrData;
    
    private String note;
}
