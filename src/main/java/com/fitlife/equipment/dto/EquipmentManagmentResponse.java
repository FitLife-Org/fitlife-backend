package com.fitlife.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentManagmentResponse {
    private String id; // maps to equipmentCode
    private String name;
    private String image; // maps to imageUrl
    private String category;
    private String area;
    private String status; // ACTIVE, MAINTENANCE, INACTIVE

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate lastMaintenance;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate nextMaintenance;

    private Integer daysToNextMaintenance;
}
