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
public class EquipmentReportBrokenRequest {
    @NotBlank(message = "Mô tả tình trạng hỏng không được để trống")
    private String description;
}
