package com.fitlife.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest {
    @NotNull(message = "Member ID is required to check-in")
    private Long memberId;
}