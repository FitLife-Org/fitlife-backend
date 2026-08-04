package com.fitlife.member.timeline.service;

import com.fitlife.member.timeline.enums.MemberTimelineType;

import java.time.LocalDateTime;

public interface MemberTimelineRecorder {
    void record(Long memberId, MemberTimelineType type, String title, String description, Long referenceId, String referenceType, String status, LocalDateTime occurredAt);

    void recordOnce(Long memberId, MemberTimelineType type, String title, String description, Long referenceId, String referenceType, String status, LocalDateTime occurredAt);
}
