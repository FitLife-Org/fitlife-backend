package com.fitlife.checkin.dto;

import com.fitlife.checkin.enums.CheckInMethod;
import com.fitlife.checkin.enums.CheckInStatus;
import lombok.*;

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
    private LocalDateTime checkOutTime;
    private CheckInMethod checkOutMethod;
    private CheckInStatus status;
    private Long checkedInBy;
    private String checkedInByName;
    private String note;
    private Boolean isInside; // Derived field: true if checked in and not yet checked out
}