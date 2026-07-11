package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInManualRequest {
    @NotBlank(message = "Member code is required")
    private String memberCode;
    
    private String note;
}
