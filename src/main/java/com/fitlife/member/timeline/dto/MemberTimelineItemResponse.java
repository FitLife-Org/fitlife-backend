package com.fitlife.member.timeline.dto;

import com.fitlife.member.timeline.enums.MemberTimelineType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberTimelineItemResponse {
    private Long id;
    private Long memberId;
    private MemberTimelineType type;
    private String title;
    private String description;
    private Long referenceId;
    private String referenceType;
    private String status;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
