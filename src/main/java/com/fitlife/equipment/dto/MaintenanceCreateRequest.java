package com.fitlife.equipment.dto;

import jakarta.validation.constraints.NotNull;
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
public class MaintenanceCreateRequest {

    @NotNull(message = "Ngày bảo trì không được để trống")
    private LocalDate maintenanceDate;

    private String maintenanceType;

    private String description;

    private BigDecimal cost;

    private String status; // SCHEDULED, COMPLETED, CANCELLED

    private Long handledById;
}
