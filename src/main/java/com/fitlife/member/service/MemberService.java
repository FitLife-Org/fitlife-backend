package com.fitlife.member.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService {
    MemberProfileResponse createMember(MemberCreationRequest request);

    String updateAvatar(String username, MultipartFile file) throws IOException;

    PageResponse<MemberProfileResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword);

    MemberProfileResponse createMemberByAdmin(MemberCreationRequest request);

    void toggleMemberLock(Long memberId);

    MemberProfileResponse getMemberById(Long memberId);

    MemberProfileResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request);

    void deleteMember(Long memberId);

    MemberProfileResponse getMyProfile(String username);

    MemberProfileResponse updateMyProfile(String username, MemberUpdateRequest request);
}
