package com.fitlife.subscription;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class SubscriptionResponse {
    private Long id;
    private Long memberId;
    private Long packageId;
    private String packageName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}