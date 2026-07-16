package com.fitlife.checkin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffManualCheckInRequest {
    private Long memberId;
    private String memberCode;
    private String reason;
}
