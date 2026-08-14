package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PeakHourItem {
    private int hour;
    private long checkInCount;
    private double percentage;
}
