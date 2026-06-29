package com.fitlife.member.service.impl;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.AdminMemberCreateRequest;
import com.fitlife.member.dto.AdminMemberUpdateRequest;
import com.fitlife.member.dto.MemberDetailResponse;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.Gender;
import com.fitlife.member.mapper.MemberMapper;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.MemberService;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.RoleRepository;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse createMemberByAdmin(AdminMemberCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập (Username) này đã tồn tại trong hệ thống!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Địa chỉ Email này đã tồn tại trong hệ thống!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        user.setStatus(com.fitlife.user.enums.UserStatus.ACTIVE);
        user.setAuthProvider(com.fitlife.user.enums.AuthProvider.LOCAL);
        Role memberRole = roleRepository.findByCode("ROLE_MEMBER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy phân quyền ROLE_MEMBER!"));
        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        String generatedMemberCode = "MEM" + String.format("%03d", (int)(Math.random() * 1000));

        Member member = new Member();
        member.setUser(savedUser);
        member.setMemberCode(generatedMemberCode);
        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setFitnessGoal(request.getFitnessGoal());

        member.setStatus(com.fitlife.member.enums.MemberStatus.ACTIVE);
        member.setIsDeleted(false);

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                member.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Định dạng giới tính không hợp lệ: " + request.getGender());
            }
        } else {
            member.setGender(null);
        }

        Member savedMember = memberRepository.save(member);

        return memberMapper.toMemberResponse(savedMember);
    }

    @Override
    @Transactional
    public MemberResponse updateMemberByAdmin(Long id, AdminMemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên mang ID: " + id));

        User user = member.getUser();

        if (!member.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Địa chỉ Email này đã tồn tại trong hệ thống!");
            }
            member.setEmail(request.getEmail());
            if (user != null) {
                user.setEmail(request.getEmail());
            }
        }

        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setFitnessGoal(request.getFitnessGoal());

        if (user != null) {
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
        }

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                member.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Định dạng giới tính không hợp lệ: " + request.getGender());
            }
        } else {
            member.setGender(null);
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                member.setStatus(com.fitlife.member.enums.MemberStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái hội viên không hợp lệ: " + request.getStatus());
            }

            if (user != null) {
                try {
                    user.setStatus(com.fitlife.user.enums.UserStatus.valueOf(request.getStatus().toUpperCase()));
                } catch (Exception e) {
                }
            }
        }

        if (user != null) {
            userRepository.save(user);
        }
        Member savedMember = memberRepository.save(member);

        return memberMapper.toMemberResponse(savedMember);
    }

    @Override
    public List<MemberResponse> getAllMembers() {
        List<Member> members = memberRepository.findByIsDeletedFalse();
        return memberMapper.toMemberResponseList(members);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse getMyProfile(String tokenIdentifier) {
        User user = userRepository.findByUsername(tokenIdentifier)
                .orElseGet(() -> userRepository.findByEmail(tokenIdentifier)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng với định danh: " + tokenIdentifier)));

        Member member = memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ hội viên"));

        return memberMapper.toProfileResponse(member);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateMyProfile(String tokenIdentifier, MemberUpdateRequest request) {
        User user = userRepository.findByUsername(tokenIdentifier)
                .orElseGet(() -> userRepository.findByEmail(tokenIdentifier)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng")));

        Member member = memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ hội viên"));

        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setFitnessGoal(request.getFitnessGoal());

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                member.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Định dạng giới tính không hợp lệ: " + request.getGender());
            }
        } else {
            member.setGender(null);
        }

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            member.setAvatarUrl(request.getAvatarUrl());
        }

        Member savedMember = memberRepository.save(member);
        return memberMapper.toProfileResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> getAllMembersForAdmin(String keyword, String status, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim().toLowerCase();
        String searchStatus = (status == null || status.trim().isEmpty() || status.equals("ALL")) ? null : status;

        List<Member> allMembers = memberRepository.findAll();

        List<Member> filteredMembers = allMembers.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsDeleted()))
                .filter(m -> searchStatus == null || m.getStatus().name().equalsIgnoreCase(searchStatus))
                .filter(m -> searchKeyword == null ||
                        (m.getFullName() != null && m.getFullName().toLowerCase().contains(searchKeyword)) ||
                        (m.getMemberCode() != null && m.getMemberCode().toLowerCase().contains(searchKeyword)) ||
                        (m.getPhone() != null && m.getPhone().contains(searchKeyword)))
                .collect(Collectors.toList());

        int totalElements = filteredMembers.size();
        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);

        List<MemberResponse> dtoList = new ArrayList<>();
        if (fromIndex < totalElements) {
            dtoList = filteredMembers.subList(fromIndex, toIndex).stream()
                    .map(memberMapper::toMemberResponse)
                    .collect(Collectors.toList());
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<MemberResponse>builder()
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(totalPages == 0 ? 1 : totalPages)
                .pageSize(pageable.getPageSize())
                .totalElements((long) totalElements)
                .data(dtoList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberDetailForAdmin(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên mang ID: " + id));
        return memberMapper.toDetailResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberByCodeForAdmin(String memberCode) {
        Member member = memberRepository.findByMemberCodeAndIsDeletedFalse(memberCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên mang mã: " + memberCode));

        return memberMapper.toDetailResponse(member);
    }
}