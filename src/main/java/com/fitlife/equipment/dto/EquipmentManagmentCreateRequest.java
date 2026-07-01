package com.fitlife.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentManagmentCreateRequest {

    @NotBlank(message = "Mã thiết bị không được để trống")
    @Size(max = 50, message = "Mã thiết bị không được vượt quá 50 ký tự")
    private String equipmentCode;

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 150, message = "Tên thiết bị không được vượt quá 150 ký tự")
    private String name;

    @Size(max = 100, message = "Danh mục không được vượt quá 100 ký tự")
    private String category;

    @Size(max = 100, message = "Khu vực không được vượt quá 100 ký tự")
    private String area;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    private String status; // ACTIVE, MAINTENANCE, INACTIVE

    private String description;

    @Size(max = 500, message = "Đường dẫn hình ảnh không được vượt quá 500 ký tự")
    private String image; // maps to imageUrl
}
