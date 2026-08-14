package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EquipmentAreaDistributionItem {
    private Long areaId;
    private String areaName;
    private long count;
}
