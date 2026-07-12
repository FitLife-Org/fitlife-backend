package com.fitlife.trainer.dto.request;

import com.fitlife.trainer.enums.TrainerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private TrainerStatus status;
}