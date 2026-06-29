package com.fitlife.member.service.impl;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.request.AdminMemberStatusUpdateRequest;
import com.fitlife.member.dto.request.MemberCreateRequest;
import com.fitlife.member.dto.request.MemberUpdateRequest;
import com.fitlife.member.dto.request.MyMemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.dto.response.MemberSummaryResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.mapper.MemberMapper;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.MemberService;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.RoleRepository;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private static final String ROLE_MEMBER_CODE = "ROLE_MEMBER";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse createMemberByAdmin(MemberCreateRequest request) {
        validateUniqueUsername(request.getUsername());
        validateUniqueEmail(request.getEmail());

        Role memberRole = roleRepository.findByCode(ROLE_MEMBER_CODE)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_MEMBER"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setIsDeleted(false);

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        Member member = new Member();
        member.setUser(savedUser);
        member.setMemberCode(generateMemberCode());
        member.setGender(request.getGender());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setAddress(request.getAddress());
        member.setEmergencyContactName(request.getEmergencyContactName());
        member.setEmergencyContactPhone(request.getEmergencyContactPhone());
        member.setJoinDate(LocalDate.now());
        member.setFitnessGoal(request.getFitnessGoal());
        member.setHealthNote(request.getHealthNote());
        member.setStatus(MemberStatus.ACTIVE);
        member.setIsDeleted(false);

        Member savedMember = memberRepository.save(member);
        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberSummaryResponse> getAllMembersForAdmin(
            String keyword,
            MemberStatus status,
            Pageable pageable
    ) {
        String searchKeyword = normalizeKeyword(keyword);

        var page = memberRepository.searchMembers(searchKeyword, status, pageable);

        return PageResponse.<MemberSummaryResponse>builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages() == 0 ? 1 : page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .data(page.getContent()
                        .stream()
                        .map(memberMapper::toSummaryResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberDetailForAdmin(Long id) {
        Member member = getActiveMemberById(id);
        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberByCodeForAdmin(String memberCode) {
        Member member = memberRepository.findByMemberCodeAndIsDeletedFalse(memberCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên với mã: " + memberCode));

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse updateMemberByAdmin(Long id, MemberUpdateRequest request) {
        Member member = getActiveMemberById(id);
        User user = member.getUser();

        updateUserInfoByAdmin(user, request);
        updateMemberInfoByAdmin(member, request);

        if (user != null) {
            userRepository.save(user);
        }

        Member savedMember = memberRepository.save(member);
        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    public MemberResponse updateMemberStatusByAdmin(Long id, AdminMemberStatusUpdateRequest request) {
        Member member = getActiveMemberById(id);

        member.setStatus(request.getStatus());

        User user = member.getUser();
        if (user != null) {
            syncUserStatusByMemberStatus(user, request.getStatus());
            userRepository.save(user);
        }

        Member savedMember = memberRepository.save(member);
        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMyProfile(String tokenIdentifier) {
        User user = findUserByUsernameOrEmail(tokenIdentifier);

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ hội viên"));

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse updateMyProfile(String tokenIdentifier, MyMemberUpdateRequest request) {
        User user = findUserByUsernameOrEmail(tokenIdentifier);

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ hội viên"));

        updateMyUserInfo(user, request);
        updateMyMemberInfo(member, request);

        userRepository.save(user);
        Member savedMember = memberRepository.save(member);

        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    public void deleteMemberByAdmin(Long id) {
        Member member = getActiveMemberById(id);

        member.setIsDeleted(true);
        member.setStatus(MemberStatus.INACTIVE);
        memberRepository.save(member);

        User user = member.getUser();
        if (user != null) {
            user.setIsDeleted(true);
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void restoreMemberByAdmin(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên với ID: " + id));

        member.setIsDeleted(false);
        member.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(member);

        User user = member.getUser();
        if (user != null) {
            user.setIsDeleted(false);
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }
    }

    private Member getActiveMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên với ID: " + id));

        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            throw new RuntimeException("Hồ sơ hội viên đã bị xóa");
        }

        return member;
    }

    private User findUserByUsernameOrEmail(String tokenIdentifier) {
        return userRepository.findByUsername(tokenIdentifier)
                .orElseGet(() -> userRepository.findByEmail(tokenIdentifier)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng")));
    }

    private void updateUserInfoByAdmin(User user, MemberUpdateRequest request) {
        if (user == null) {
            return;
        }

        if (request.getEmail() != null && !Objects.equals(request.getEmail(), user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
    }

    private void updateMemberInfoByAdmin(Member member, MemberUpdateRequest request) {
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            member.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }

        if (request.getEmergencyContactName() != null) {
            member.setEmergencyContactName(request.getEmergencyContactName());
        }

        if (request.getEmergencyContactPhone() != null) {
            member.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        if (request.getFitnessGoal() != null) {
            member.setFitnessGoal(request.getFitnessGoal());
        }

        if (request.getHealthNote() != null) {
            member.setHealthNote(request.getHealthNote());
        }

        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }
    }

    private void updateMyUserInfo(User user, MyMemberUpdateRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
    }

    private void updateMyMemberInfo(Member member, MyMemberUpdateRequest request) {
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            member.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }

        if (request.getEmergencyContactName() != null) {
            member.setEmergencyContactName(request.getEmergencyContactName());
        }

        if (request.getEmergencyContactPhone() != null) {
            member.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        if (request.getFitnessGoal() != null) {
            member.setFitnessGoal(request.getFitnessGoal());
        }

        if (request.getHealthNote() != null) {
            member.setHealthNote(request.getHealthNote());
        }
    }

    private void syncUserStatusByMemberStatus(User user, MemberStatus memberStatus) {
        if (memberStatus == MemberStatus.ACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            user.setIsDeleted(false);
            return;
        }

        if (memberStatus == MemberStatus.INACTIVE || memberStatus == MemberStatus.SUSPENDED) {
            user.setStatus(UserStatus.INACTIVE);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }

    private String generateMemberCode() {
        String code;

        do {
            code = "MEM" + System.currentTimeMillis();
        } while (memberRepository.existsByMemberCode(code));

        return code;
    }
}