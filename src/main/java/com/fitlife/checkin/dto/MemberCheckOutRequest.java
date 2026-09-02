package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCheckOutRequest {
    @NotBlank(message = "QR Token is required")
    private String qrToken;
}
