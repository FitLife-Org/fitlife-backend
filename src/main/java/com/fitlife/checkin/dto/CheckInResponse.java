package com.fitlife.checkin.dto;

import com.fitlife.checkin.enums.CheckInMethod;
import com.fitlife.checkin.enums.CheckInStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private Long id;
    private Long memberId;
    private String memberCode;
    private String memberName;
    private Long subscriptionId;
    private String packageName;
    private LocalDateTime checkInTime;
    private CheckInMethod checkInMethod;
    private CheckInStatus status;
    private Long checkedInBy;
    private String checkedInByName;
    private String note;
}