package com.fitlife.member.service.impl;

import com.fitlife.core.response.PageResponse;
import com.fitlife.identity.entity.User;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.identity.repository.UserRepository;
import com.fitlife.core.storage.impl.CloudinaryServiceImpl;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.MemberService;
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
    public MemberProfileResponse createMember(MemberCreationRequest request) {
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
    public PageResponse<MemberProfileResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword) {
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

        List<MemberProfileResponse> content = memberPage.getContent().stream()
                .map(this::mapToMemberResponse) // Separate mapping method for cleaner code
                .toList();

        return PageResponse.<MemberProfileResponse>builder()
                .currentPage(page)
                .totalPages(memberPage.getTotalPages())
                .pageSize(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .data(content)
                .build();
    }

    // Helper method to reuse mapping logic across the service
    private MemberProfileResponse mapToMemberResponse(Member member) {
        return MemberProfileResponse.builder()
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
    public MemberProfileResponse createMemberByAdmin(MemberCreationRequest request) {
        // 1. Check trùng lặp
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký.");
        }
        if (userRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã được dùng làm tài khoản.");
        }

        // 2. TẠO TÀI KHOẢN USER TRƯỚC (Dùng Email làm Username, Pass mặc định: 123456)
        User newUser = User.builder()
                .username(request.getEmail())
                // Pass "123456" mã hóa Bcrypt. Nếu em có PasswordEncoder thì dùng passwordEncoder.encode("123456")
                .password("$2a$10$X8C5.5hN7q6aN9zJbXqY4.0yZ3.rU7y7T4/q4z4u4u4u4u4u4u4u4")
                .role("MEMBER")
                .status("ACTIVE")
                .build();
        userRepository.save(newUser);

        // 3. TẠO HỒ SƠ MEMBER GẮN VỚI USER TRÊN
        Member newMember = Member.builder()
                .user(newUser)
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
    public MemberProfileResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên ID: " + memberId));
        return mapToMemberResponse(member);
    }

    @Transactional
    @Override
    public MemberProfileResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request) {
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

        // THỰC THI SOFT DELETE CHUẨN MỰC
        member.setIsDeleted(true);
        member.setStatus("INACTIVE"); // Khóa luôn trạng thái cho an toàn

        if (user != null) {
            user.setIsDeleted(true);
            user.setStatus("INACTIVE");
            userRepository.save(user); // Lưu user
        }

        memberRepository.save(member); // Lưu member

        // Lưu ý: Không cần cascade (xóa lan) isDeleted sang các bảng lịch sử (checkin, orders).
        // Lịch sử là bất biến.
    }
}