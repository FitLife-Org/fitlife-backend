package com.fitlife.service;

import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService {
    MemberResponse createMember(MemberCreationRequest request);

    String updateAvatar(String username, MultipartFile file) throws IOException;

    PageResponse<MemberResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword);

    void toggleMemberLock(Long memberId);

    MemberResponse getMemberById(Long memberId);

    MemberResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request);

    void deleteMember(Long memberId);
}
