package com.fitlife.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentManagmentStatusUpdateRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // ACTIVE, MAINTENANCE, INACTIVE
}
