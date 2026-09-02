package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCheckInRequest {
    private String qrToken;
    private String qrCodeData;

    public String getQrToken() {
        return qrToken != null ? qrToken : qrCodeData;
    }
}
