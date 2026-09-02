package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {

    @Schema(description = "Full name", example = "FitLife User")
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than or equal to 100 characters")
    private String fullName;

    @Schema(description = "Phone number", example = "0900000022")
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Phone number is invalid")
    private String phone;
}
