package com.fitlife.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentManagmentSummaryResponse {
    private long total;
    private StatDetail active;
    private StatDetail maintenance;
    private StatDetail inactive;
    private UpcomingMaintenanceDetail upcomingMaintenance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatDetail {
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingMaintenanceDetail {
        private long count;
        private String timeFrame;
    }
}
