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
public class CheckInTodayStatisticsResponse {
    private LocalDate date;
    private long totalCheckIns;
    private long manualCheckIns;
    private long qrCheckIns;
    private long cancelledCheckIns;
}
