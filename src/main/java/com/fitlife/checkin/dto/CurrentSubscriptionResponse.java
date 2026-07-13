package com.fitlife.checkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
