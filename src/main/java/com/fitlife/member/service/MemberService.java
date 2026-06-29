package com.fitlife.member.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.request.AdminMemberStatusUpdateRequest;
import com.fitlife.member.dto.request.MemberCreateRequest;
import com.fitlife.member.dto.request.MemberUpdateRequest;
import com.fitlife.member.dto.request.MyMemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.dto.response.MemberSummaryResponse;
import com.fitlife.member.enums.MemberStatus;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberResponse createMemberByAdmin(MemberCreateRequest request);

    PageResponse<MemberSummaryResponse> getAllMembersForAdmin(
            String keyword,
            MemberStatus status,
            Pageable pageable
    );

    MemberResponse getMemberDetailForAdmin(Long id);

    MemberResponse getMemberByCodeForAdmin(String memberCode);

    MemberResponse updateMemberByAdmin(Long id, MemberUpdateRequest request);

    MemberResponse updateMemberStatusByAdmin(Long id, AdminMemberStatusUpdateRequest request);

    MemberResponse getMyProfile(String username);

    MemberResponse updateMyProfile(String username, MyMemberUpdateRequest request);

    void deleteMemberByAdmin(Long id);

    void restoreMemberByAdmin(Long id);
}