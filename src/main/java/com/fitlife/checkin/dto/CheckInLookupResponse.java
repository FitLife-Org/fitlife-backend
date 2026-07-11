package com.fitlife.checkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInLookupResponse {
    private Long memberId;
    private String memberCode;
    private String fullName;
    private String email;
    private String phone;
    private String userStatus;
    private CurrentSubscriptionResponse currentSubscription;
    private Boolean canCheckIn;
    private String checkInMessage;
}
