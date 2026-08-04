package com.fitlife.member.timeline.repository;

import com.fitlife.member.timeline.entity.MemberTimelineEvent;
import com.fitlife.member.timeline.enums.MemberTimelineType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTimelineEventRepository extends JpaRepository<MemberTimelineEvent, Long> {
    Page<MemberTimelineEvent> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndReferenceTypeAndReferenceIdAndType(Long memberId, String referenceType, Long referenceId, MemberTimelineType type);
}
