package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffMemberQrCheckInRequest {
    private String qrData;
    private String memberQrCode;
    private String reason;

    public String getQrData() {
        return qrData != null ? qrData : memberQrCode;
    }
}
