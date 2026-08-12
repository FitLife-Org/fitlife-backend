package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class EquipmentSummaryResponse {
    private long totalEquipment;
    private long activeEquipmentCount;
    private long maintenanceEquipmentCount;
    private long brokenEquipmentCount; // Dùng INACTIVE làm broken
    private List<EquipmentAreaDistributionItem> areaDistribution;
}
