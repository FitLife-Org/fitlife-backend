package com.fitlife.member.timeline.service.impl;

import com.fitlife.common.exception.*;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.timeline.dto.MemberTimelineItemResponse;
import com.fitlife.member.timeline.repository.MemberTimelineEventRepository;
import com.fitlife.member.timeline.service.MemberTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTimelineServiceImpl implements MemberTimelineService {
    private final MemberRepository members;
    private final MemberTimelineEventRepository events;

    public PageResponse<MemberTimelineItemResponse> getTimeline(Long memberId, Pageable pageable) {
        if (!members.existsById(memberId)) throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        return PageResponse.from(events.findByMemberId(memberId, pageable), e -> MemberTimelineItemResponse.builder().id(e.getId()).memberId(e.getMember().getId()).type(e.getType()).title(e.getTitle()).description(e.getDescription()).referenceId(e.getReferenceId()).referenceType(e.getReferenceType()).status(e.getStatus()).occurredAt(e.getOccurredAt()).createdAt(e.getCreatedAt()).build());
    }
}
