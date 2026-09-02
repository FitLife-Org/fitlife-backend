package com.fitlife.trainer.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;

@Getter
@Setter
public class TrainerUpdateRequest {
    private String specialization;

    @Min(value = 0, message = "Experience years must be >= 0")
    private Integer experienceYears;

    private String certifications;
    private String bio;
}