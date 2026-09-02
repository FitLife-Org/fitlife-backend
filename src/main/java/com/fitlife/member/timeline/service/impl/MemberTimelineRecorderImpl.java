package com.fitlife.member.timeline.service.impl;

import com.fitlife.common.exception.*;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.timeline.entity.MemberTimelineEvent;
import com.fitlife.member.timeline.enums.MemberTimelineType;
import com.fitlife.member.timeline.repository.MemberTimelineEventRepository;
import com.fitlife.member.timeline.service.MemberTimelineRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberTimelineRecorderImpl implements MemberTimelineRecorder {
    private final MemberRepository members;
    private final MemberTimelineEventRepository events;

    @Transactional
    public void record(Long memberId, MemberTimelineType type, String title, String description, Long referenceId, String referenceType, String status, LocalDateTime occurredAt) {
        var member = members.findById(memberId).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        events.save(MemberTimelineEvent.builder().member(member).type(type).title(title == null || title.isBlank() ? "Activity" : title.trim()).description(description).referenceId(referenceId).referenceType(referenceType).status(status).occurredAt(occurredAt).build());
    }

    @Transactional
    public void recordOnce(Long memberId, MemberTimelineType type, String title, String description, Long referenceId, String referenceType, String status, LocalDateTime occurredAt) {
        if (referenceId != null && referenceType != null && events.existsByMemberIdAndReferenceTypeAndReferenceIdAndType(memberId, referenceType, referenceId, type))
            return;
        record(memberId, type, title, description, referenceId, referenceType, status, occurredAt);
    }
}
