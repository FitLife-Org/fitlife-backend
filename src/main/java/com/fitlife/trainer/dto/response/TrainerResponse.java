package com.fitlife.trainer.dto.response;

import com.fitlife.trainer.enums.TrainerStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String trainerCode;
    private String specialization;
    private Integer experienceYears;
    private String certifications;
    private String bio;
    private String avatarUrl;
    private TrainerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}