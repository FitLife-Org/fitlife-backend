package com.fitlife.member.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.MemberDetailResponse;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {
    MemberProfileResponse getMyProfile(String username);
    MemberProfileResponse updateMyProfile(String username, MemberUpdateRequest request);
    PageResponse<MemberResponse> getAllMembersForAdmin(String keyword, String status, Pageable pageable);
    MemberDetailResponse getMemberDetailForAdmin(Long id);

    List<MemberResponse> getAllMembers();

}