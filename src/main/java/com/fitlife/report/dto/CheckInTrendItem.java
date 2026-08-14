package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckInTrendItem {
    private String period;
    private long checkInCount;
}
