package com.fitlife.checkin.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSubscriptionResponse {
    private Long subscriptionId;
    private String packageName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
