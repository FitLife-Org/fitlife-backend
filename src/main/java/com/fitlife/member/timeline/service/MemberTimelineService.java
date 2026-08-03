package com.fitlife.member.timeline.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.timeline.dto.MemberTimelineItemResponse;
import org.springframework.data.domain.Pageable;

public interface MemberTimelineService {
    PageResponse<MemberTimelineItemResponse> getTimeline(Long memberId, Pageable pageable);
}
