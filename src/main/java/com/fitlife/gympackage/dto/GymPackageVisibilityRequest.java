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
    private String status;
    private Boolean active;

    public String getStatus() {
        if (status != null) return status;
        if (active != null) {
            return Boolean.TRUE.equals(active) ? "ACTIVE" : "INACTIVE";
        }
        return null;
    }
}
