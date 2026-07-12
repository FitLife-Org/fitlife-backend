package com.fitlife.trainer.dto.request;

import com.fitlife.trainer.enums.TrainerStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequest {

    @NotBlank(message = "Specialization cannot be blank")
    private String specialization;

    @Min(value = 0, message = "Experience years must be greater than or equal to 0")
    private Integer experienceYears;

    private String certifications;
    private String bio;
    private TrainerStatus status;
}