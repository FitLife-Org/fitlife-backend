package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AgeGroupDistributionItem {
    private String ageGroup;
    private long count;
}
