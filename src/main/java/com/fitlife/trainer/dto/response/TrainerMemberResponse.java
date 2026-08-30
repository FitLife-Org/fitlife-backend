package com.fitlife.trainer.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMemberResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String packageName;
    private String status;
    private Integer sessionsTotal;
    private Integer sessionsCompleted;
    private LocalDate joinDate;
}
