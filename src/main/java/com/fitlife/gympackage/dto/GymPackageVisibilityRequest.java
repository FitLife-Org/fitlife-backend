package com.fitlife.gympackage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPackageVisibilityRequest {
    @NotBlank(message = "Trạng thái hiển thị không được để trống")
    private String status; // ACTIVE, INACTIVE
}
