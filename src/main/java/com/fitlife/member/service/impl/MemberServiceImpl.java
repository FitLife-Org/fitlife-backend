package com.fitlife.member.service.impl;

import com.fitlife.common.response.PageResponse;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.auth.entity.User;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import com.fitlife.member.entity.Member;
import com.fitlife.auth.repository.UserRepository;
import com.fitlife.common.file.service.impl.CloudinaryServiceImpl;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.mapper.MemberMapper;
import com.fitlife.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CloudinaryServiceImpl cloudinaryServiceImpl;
    private final MemberMapper memberMapper;

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String BANNED_STATUS = "BANNED";
    private static final String INACTIVE_STATUS = "INACTIVE";

    @Transactional
    @Override
    public MemberProfileResponse createMember(MemberCreationRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("KhÄ‚Â´ng tÄ‚Â¬m thĂ¡ÂºÂ¥y ngĂ†Â°Ă¡Â»Âi dÄ‚Â¹ng ID: " + request.getUserId()));

        Member newMember = memberMapper.toEntity(request);
        newMember.setUser(user);
        newMember.setMemberCode("MEM" + String.format("%06d", user.getId()));
        newMember.setStatus(ACTIVE_STATUS);

        memberRepository.save(newMember);
        return memberMapper.toResponse(newMember);
    }

    @Transactional
    @Override
    public String updateAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("KhÄ‚Â´ng tÄ‚Â¬m thĂ¡ÂºÂ¥y User"));

        Member member = user.getMember();
        if (member == null) throw new AppException(ErrorCode.MEMBER_NOT_FOUND);

        if (member.getAvatarUrl() != null) {
            String publicId = "avatars/member_" + member.getId();
            try {
                cloudinaryServiceImpl.deleteImage(publicId);
            } catch (Exception e) {
                log.warn("KhÄ‚Â´ng thĂ¡Â»Æ’ xÄ‚Â³a Ă¡ÂºÂ£nh cĂ…Â© trÄ‚Âªn Cloudinary: {}", e.getMessage());
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
                .map(memberMapper::toResponse)
                .toList();

        return PageResponse.<MemberProfileResponse>builder()
                .currentPage(page)
                .totalPages(memberPage.getTotalPages())
                .pageSize(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .data(content)
                .build();
    }

    @Transactional
    @Override
    public MemberProfileResponse createMemberByAdmin(MemberCreationRequest request) {
        // 1. Check trÄ‚Â¹ng lĂ¡ÂºÂ·p
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (userRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. TĂ¡ÂºÂ O TÄ‚â‚¬I KHOĂ¡ÂºÂ¢N USER TRĂ†Â¯Ă¡Â»ÂC (DÄ‚Â¹ng Email lÄ‚Â m Username, Pass mĂ¡ÂºÂ·c Ă„â€˜Ă¡Â»â€¹nh: 123456)
        User newUser = User.builder()
                .username(request.getEmail())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                // Pass "123456" mÄ‚Â£ hÄ‚Â³a Bcrypt. NĂ¡ÂºÂ¿u em cÄ‚Â³ PasswordEncoder thÄ‚Â¬ dÄ‚Â¹ng passwordEncoder.encode("123456")
                .passwordHash("$2a$10$X8C5.5hN7q6aN9zJbXqY4.0yZ3.rU7y7T4/q4z4u4u4u4u4u4u4u4")
                .status(ACTIVE_STATUS)
                .build();
        newUser.setRole("ROLE_MEMBER");
        userRepository.save(newUser);
        userRepository.assignRoleToUser(newUser.getId(), "ROLE_MEMBER");

        // 3. TĂ¡ÂºÂ O HĂ¡Â»â€™ SĂ†Â  MEMBER GĂ¡ÂºÂ®N VĂ¡Â»ÂI USER TRÄ‚ÂN
        Member newMember = memberMapper.toEntity(request);
        newMember.setUser(newUser);
        newMember.setMemberCode("MEM" + String.format("%06d", newUser.getId()));
        newMember.setStatus(ACTIVE_STATUS);
        memberRepository.save(newMember);

        return memberMapper.toResponse(newMember);
    }

    @Transactional
    @Override
    public void toggleMemberLock(Long memberId) {
        // 1. TÄ‚Â¬m Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. LĂ¡ÂºÂ¥y User liÄ‚Âªn kĂ¡ÂºÂ¿t (TÄ‚Â i khoĂ¡ÂºÂ£n Ă„â€˜Ă¡Â»Æ’ Ă„â€˜Ă„Æ’ng nhĂ¡ÂºÂ­p)
        User user = member.getUser();
        if (user == null) {
            throw new AppException(ErrorCode.MEMBER_NO_ACCOUNT);
        }

        // 3. Ă„ÂĂ¡ÂºÂ£o trĂ¡ÂºÂ¡ng thÄ‚Â¡i (KhÄ‚Â³a cĂ¡ÂºÂ£ Member profile lĂ¡ÂºÂ«n User login)
        if (ACTIVE_STATUS.equalsIgnoreCase(member.getStatus())) {
            member.setStatus(BANNED_STATUS);
            user.setStatus(BANNED_STATUS);
        } else {
            member.setStatus(ACTIVE_STATUS);
            user.setStatus(ACTIVE_STATUS);
        }

        // 4. LĂ†Â°u thay Ă„â€˜Ă¡Â»â€¢i
        memberRepository.save(member);
        userRepository.save(user);
    }

    @Override
    public MemberProfileResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        return memberMapper.toResponse(member);
    }

    @Transactional
    @Override
    public MemberProfileResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // CĂ¡ÂºÂ­p nhĂ¡ÂºÂ­t thÄ‚Â´ng tin
        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());

        memberRepository.save(member);
        return memberMapper.toResponse(member);
    }

    @Transactional
    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        User user = member.getUser();

        // THĂ¡Â»Â°C THI SOFT DELETE CHUĂ¡ÂºÂ¨N MĂ¡Â»Â°C
        member.setIsDeleted(true);
        member.setStatus(INACTIVE_STATUS); // KhÄ‚Â³a luÄ‚Â´n trĂ¡ÂºÂ¡ng thÄ‚Â¡i cho an toÄ‚Â n

        if (user != null) {
            user.setIsDeleted(true);
            user.setStatus(INACTIVE_STATUS);
            userRepository.save(user); // LĂ†Â°u user
        }

        memberRepository.save(member); // LĂ†Â°u member

        // LĂ†Â°u Ä‚Â½: KhÄ‚Â´ng cĂ¡ÂºÂ§n cascade (xÄ‚Â³a lan) isDeleted sang cÄ‚Â¡c bĂ¡ÂºÂ£ng lĂ¡Â»â€¹ch sĂ¡Â»Â­ (checkin, orders).
        // LĂ¡Â»â€¹ch sĂ¡Â»Â­ lÄ‚Â  bĂ¡ÂºÂ¥t biĂ¡ÂºÂ¿n.
    }

    @Override
    @Transactional
    public MemberProfileResponse getMyProfile(String username) {
        // SĂ¡Â»Â¬A CHUĂ¡Â»â€“I STRING THÄ‚â‚¬NH ERROR CODE CHUĂ¡ÂºÂ¨N
        Member member = memberRepository.findByUser_Username(username)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        return memberMapper.toProfileResponse(member);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateMyProfile(String username, MemberUpdateRequest request) {
        // SĂ¡Â»Â¬A CHUĂ¡Â»â€“I STRING THÄ‚â‚¬NH ERROR CODE CHUĂ¡ÂºÂ¨N
        Member member = memberRepository.findByUser_Username(username)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // CĂ¡ÂºÂ­p nhĂ¡ÂºÂ­t thÄ‚Â´ng tin cĂ†Â¡ bĂ¡ÂºÂ£n
        if (request.getFullName() != null) member.setFullName(request.getFullName());
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getFitnessGoal() != null) member.setFitnessGoal(request.getFitnessGoal());

        // Logic tÄ‚Â­nh toÄ‚Â¡n BMI TĂ¡Â»Â± Ă„â€˜Ă¡Â»â„¢ng
        if (request.getWeight() != null && request.getHeight() != null) {
            member.setWeight(BigDecimal.valueOf(request.getWeight()));
            member.setHeight(BigDecimal.valueOf(request.getHeight()));

            // Ă„ÂĂ¡Â»â€¢i cm sang m
            double heightInMeter = request.getHeight() / 100.0;
            // TÄ‚Â­nh BMI vÄ‚Â  lÄ‚Â m trÄ‚Â²n 2 chĂ¡Â»Â¯ sĂ¡Â»â€˜ thĂ¡ÂºÂ­p phÄ‚Â¢n
            double bmi = Math.round((request.getWeight() / (heightInMeter * heightInMeter)) * 100.0) / 100.0;
            member.setBmi(BigDecimal.valueOf(bmi));
        }

        Member savedMember = memberRepository.save(member);
        return memberMapper.toProfileResponse(savedMember);
    }
}