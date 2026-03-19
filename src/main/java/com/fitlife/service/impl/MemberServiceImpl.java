package com.fitlife.service.impl;

import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse;
import com.fitlife.entity.Member;
import com.fitlife.entity.User;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.UserRepository;
import com.fitlife.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CloudinaryServiceImpl cloudinaryServiceImpl;

    @Transactional
    @Override
    public MemberResponse createMember(MemberCreationRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            // Keep Exception messages in Vietnamese for the End-user
            throw new RuntimeException("Số điện thoại đã được đăng ký.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + request.getUserId()));

        Member newMember = Member.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .build();

        memberRepository.save(newMember);
        return mapToMemberResponse(newMember);
    }

    @Transactional
    @Override
    public String updateAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        Member member = user.getMember();
        if (member == null) throw new RuntimeException("Không tìm thấy thông tin hội viên");

        if (member.getAvatarUrl() != null) {
            String publicId = "avatars/member_" + member.getId();
            try {
                cloudinaryServiceImpl.deleteImage(publicId);
            } catch (Exception e) {
                System.err.println("Không thể xóa ảnh cũ trên Cloudinary: " + e.getMessage());
            }
        }

        // Upload new photo
        String avatarUrl = cloudinaryServiceImpl.uploadImage(file, "avatars", "member_" + member.getId());
        member.setAvatarUrl(avatarUrl);

        return avatarUrl;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MemberResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword) {
        // Protect pagination logic: Ensure page is never < 1
        int pageIndex = Math.max(0, page - 1);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageIndex, size, sort);
        Page<Member> memberPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            memberPage = memberRepository.findByFullNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            memberPage = memberRepository.findAll(pageable);
        }

        List<MemberResponse> content = memberPage.getContent().stream()
                .map(this::mapToMemberResponse) // Separate mapping method for cleaner code
                .toList();

        return PageResponse.<MemberResponse>builder()
                .currentPage(page)
                .totalPages(memberPage.getTotalPages())
                .pageSize(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .data(content)
                .build();
    }

    // Helper method to reuse mapping logic across the service
    private MemberResponse mapToMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser() != null ? member.getUser().getId() : null)
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .status(member.getStatus())
                .avatarUrl(member.getAvatarUrl())
                .build();
    }

    @Transactional
    @Override
    public void toggleMemberLock(Long memberId) {
        // 1. Tìm Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên ID: " + memberId));

        // 2. Lấy User liên kết (Tài khoản để đăng nhập)
        User user = member.getUser();
        if (user == null) {
            throw new RuntimeException("Hội viên chưa có tài khoản đăng nhập");
        }

        // 3. Đảo trạng thái (Khóa cả Member profile lẫn User login)
        if ("ACTIVE".equalsIgnoreCase(member.getStatus())) {
            member.setStatus("BANNED");
            user.setStatus("BANNED");
        } else {
            member.setStatus("ACTIVE");
            user.setStatus("ACTIVE");
        }

        // 4. Lưu thay đổi
        memberRepository.save(member);
        userRepository.save(user);
    }

    @Override
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên ID: " + memberId));
        return mapToMemberResponse(member);
    }

    @Transactional
    @Override
    public MemberResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên ID: " + memberId));

        // Cập nhật thông tin
        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());

        memberRepository.save(member);
        return mapToMemberResponse(member);
    }

    @Transactional
    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên ID: " + memberId));

        User user = member.getUser();

        // Xóa hội viên trước, xóa user sau (tùy thuộc vào thiết kế khóa ngoại của em)
        memberRepository.delete(member);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}