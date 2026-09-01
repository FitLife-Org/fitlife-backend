package com.fitlife.trainer.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerAssignmentRequestResponse {
    private Long assignmentId;
    private Long memberId;
    private String memberCode;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String packageName;
    private String requestType; // "NEW_ASSIGNMENT", "CANCEL_ASSIGNMENT"
    private String status; // "PENDING", "PENDING_CANCEL"
    private LocalDateTime createdAt;
}
