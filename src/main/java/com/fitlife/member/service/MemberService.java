package com.fitlife.member.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {


    MemberResponse createMemberByAdmin(AdminMemberCreateRequest request);
    MemberProfileResponse getMyProfile(String username);
    MemberProfileResponse updateMyProfile(String username, MemberUpdateRequest request);
    PageResponse<MemberResponse> getAllMembersForAdmin(String keyword, String status, Pageable pageable);
    MemberDetailResponse getMemberDetailForAdmin(Long id);
    MemberDetailResponse getMemberByCodeForAdmin(String memberCode);
    List<MemberResponse> getAllMembers();
    MemberResponse updateMemberByAdmin(Long id, AdminMemberUpdateRequest request);
    MemberResponse updateMemberStatusByAdmin(Long id, AdminMemberStatusUpdateRequest request);
}