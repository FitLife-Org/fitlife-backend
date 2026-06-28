package com.fitlife.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceScheduleResponse {
    private Long id;
    private String equipmentId; // maps to equipmentCode
    private String equipmentName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate maintenanceDate;

    private String maintenanceType;
    private String description;
    private BigDecimal cost;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private Long handledById;
    private String handledByName;
}
