package com.fitlife.trainer.dto.request;

import com.fitlife.trainer.enums.TrainerStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainerCreateRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Trainer code is required")
    private String trainerCode;

    private String specialization;

    @Min(value = 0, message = "Experience years must be >= 0")
    private Integer experienceYears;

    private String certifications;
    private String bio;
    private TrainerStatus status;
}