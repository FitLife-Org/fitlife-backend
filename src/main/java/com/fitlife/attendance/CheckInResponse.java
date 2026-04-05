package com.fitlife.attendance;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CheckInResponse {
    private Long memberId;
    private String memberName;
    private LocalDateTime checkInTime;
    private String status; // "ACCESS_GRANTED" hoặc "ACCESS_DENIED"
    private String message; // Reason details
}