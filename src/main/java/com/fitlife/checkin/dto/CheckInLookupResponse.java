package com.fitlife.checkin.dto;

import lombok.*;

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
