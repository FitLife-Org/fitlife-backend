package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCheckInQrRequest {
    @NotBlank(message = "Name of QR point is required")
    private String name;

    private String location;

    private Boolean active;
}
