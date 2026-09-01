package com.fitlife.trainer.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSessionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private String status;
    private String notes;
}
