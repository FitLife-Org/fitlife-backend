package com.fitlife.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInCancelRequest {
    @NotBlank(message = "Reason for cancellation is required")
    private String reason;
}
