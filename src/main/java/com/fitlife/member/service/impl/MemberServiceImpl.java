package com.fitlife.member.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.avatar.service.MemberAvatarStorageService;
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
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.member.service.MemberService;
import com.fitlife.member.timeline.enums.MemberTimelineType;
import com.fitlife.member.timeline.service.MemberTimelineRecorder;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl
        implements MemberService {

    private static final String ROLE_MEMBER_CODE =
            "ROLE_MEMBER";

    private final MemberRepository memberRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final MemberMapper memberMapper;

    private final PasswordEncoder passwordEncoder;

    private final CurrentMemberService currentMemberService;

    private final MemberAvatarStorageService
            memberAvatarStorageService;

    private final MemberTimelineRecorder
            memberTimelineRecorder;

    private final com.fitlife.trainer.repository.TrainerRepository trainerRepository;
    private final com.fitlife.trainer.mapper.TrainerMapper trainerMapper;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public MemberResponse createMemberByAdmin(
            MemberCreateRequest request
    ) {
        validateDateOfBirth(
                request.getDateOfBirth()
        );

        String username =
                normalizeRequired(
                        request.getUsername()
                );

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        String phone =
                normalizeNullable(
                        request.getPhone()
                );

        validateUniqueUsername(
                username,
                null
        );

        validateUniqueEmail(
                email,
                null
        );

        validateUniquePhone(
                phone,
                null
        );

        Role memberRole =
                roleRepository
                        .findByCode(
                                ROLE_MEMBER_CODE
                        )
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                ErrorCode.ROLE_NOT_FOUND
                                        )
                        );

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "123456";
        }

        User user =
                User.builder()
                        .username(username)
                        .email(email)
                        .passwordHash(
                                passwordEncoder.encode(
                                        rawPassword
                                )
                        )
                        .fullName(
                                normalizeRequired(
                                        request.getFullName()
                                )
                        )
                        .phone(phone)
                        .status(
                                UserStatus.ACTIVE
                        )
                        .authProvider(
                                AuthProvider.LOCAL
                        )
                        .emailVerified(true)
                        .isDeleted(false)
                        .roles(
                                new HashSet<>()
                        )
                        .build();

        user.getRoles().add(
                memberRole
        );

        User savedUser =
                userRepository.save(
                        user
                );

        Member member =
                Member.builder()
                        .user(savedUser)
                        .memberCode(
                                generateMemberCode()
                        )
                        .gender(
                                request.getGender()
                        )
                        .dateOfBirth(
                                request.getDateOfBirth()
                        )
                        .address(
                                normalizeNullable(
                                        request.getAddress()
                                )
                        )
                        .emergencyContactName(
                                normalizeNullable(
                                        request
                                                .getEmergencyContactName()
                                )
                        )
                        .emergencyContactPhone(
                                normalizeNullable(
                                        request
                                                .getEmergencyContactPhone()
                                )
                        )
                        .joinDate(
                                LocalDate.now()
                        )
                        .fitnessGoal(
                                request.getFitnessGoal()
                        )
                        .healthNote(
                                normalizeNullable(
                                        request.getHealthNote()
                                )
                        )
                        .status(
                                MemberStatus.ACTIVE
                        )
                        .isDeleted(false)
                        .build();

        Member savedMember =
                memberRepository.save(
                        member
                );

        memberTimelineRecorder.recordOnce(
                savedMember.getId(),
                MemberTimelineType.SYSTEM,
                "Tạo hồ sơ hội viên",
                "Hồ sơ hội viên đã được tạo bởi quản trị viên.",
                savedMember.getId(),
                "MEMBER",
                savedMember
                        .getStatus()
                        .name(),
                LocalDateTime.now()
        );

        return memberMapper.toResponse(
                savedMember
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberSummaryResponse>
    getAllMembersForAdmin(
            String keyword,
            MemberStatus status,
            Pageable pageable
    ) {
        var page =
                memberRepository.searchMembers(
                        normalizeNullable(
                                keyword
                        ),
                        status,
                        pageable
                );

        return PageResponse.from(
                page,
                memberMapper::toSummaryResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberDetailForAdmin(
            Long id
    ) {
        return memberMapper.toResponse(
                getActiveMemberById(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberByCodeForAdmin(
            String memberCode
    ) {
        String normalizedCode =
                normalizeRequired(
                        memberCode
                ).toUpperCase(
                        Locale.ROOT
                );

        Member member =
                memberRepository
                        .findByMemberCodeAndIsDeletedFalse(
                                normalizedCode
                        )
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                ErrorCode.MEMBER_NOT_FOUND
                                        )
                        );

        return memberMapper.toResponse(
                member
        );
    }

    @Override
    @Transactional
    public MemberResponse updateMemberByAdmin(
            Long id,
            MemberUpdateRequest request
    ) {
        validateDateOfBirth(
                request.getDateOfBirth()
        );

        Member member =
                getActiveMemberById(
                        id
                );

        User user =
                requireLinkedUser(
                        member
                );

        updateUserInfoByAdmin(
                user,
                request
        );

        updateMemberInfoByAdmin(
                member,
                request
        );

        userRepository.save(
                user
        );

        Member savedMember =
                memberRepository.save(
                        member
                );

        memberTimelineRecorder.record(
                savedMember.getId(),
                MemberTimelineType.MEMBER_PROFILE,
                "Quản trị viên cập nhật hồ sơ",
                "Thông tin hội viên đã được cập nhật.",
                savedMember.getId(),
                "MEMBER",
                savedMember
                        .getStatus()
                        .name(),
                LocalDateTime.now()
        );

        return memberMapper.toResponse(
                savedMember
        );
    }

    @Override
    @Transactional
    public MemberResponse updateMemberStatusByAdmin(
            Long id,
            AdminMemberStatusUpdateRequest request
    ) {
        if (
                request.getStatus() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Member member =
                getActiveMemberById(
                        id
                );

        User user =
                requireLinkedUser(
                        member
                );

        member.setStatus(
                request.getStatus()
        );

        syncUserStatusByMemberStatus(
                user,
                request.getStatus()
        );

        userRepository.save(
                user
        );

        Member savedMember =
                memberRepository.save(
                        member
                );

        memberTimelineRecorder.record(
                savedMember.getId(),
                MemberTimelineType.SYSTEM,
                "Cập nhật trạng thái hội viên",
                "Trạng thái hội viên đã được thay đổi.",
                savedMember.getId(),
                "MEMBER_STATUS",
                savedMember
                        .getStatus()
                        .name(),
                LocalDateTime.now()
        );

        return memberMapper.toResponse(
                savedMember
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMyProfile() {
        return memberMapper.toResponse(
                currentMemberService
                        .getCurrentMember()
        );
    }

    @Override
    @Transactional
    public MemberResponse updateMyProfile(
            MyMemberUpdateRequest request
    ) {
        validateDateOfBirth(
                request.getDateOfBirth()
        );

        Member member =
                currentMemberService
                        .getCurrentMember();

        User user =
                requireLinkedUser(
                        member
                );

        updateMyUserInfo(
                user,
                request
        );

        updateMyMemberInfo(
                member,
                request
        );

        userRepository.save(
                user
        );

        Member savedMember =
                memberRepository.save(
                        member
                );

        memberTimelineRecorder.record(
                savedMember.getId(),
                MemberTimelineType.MEMBER_PROFILE,
                "Cập nhật hồ sơ",
                "Hội viên đã cập nhật thông tin cá nhân.",
                savedMember.getId(),
                "MEMBER",
                savedMember
                        .getStatus()
                        .name(),
                LocalDateTime.now()
        );

        return memberMapper.toResponse(
                savedMember
        );
    }

    @Override
    @Transactional
    public MemberResponse updateMyAvatar(
            MultipartFile file
    ) {
        Member member =
                currentMemberService
                        .getCurrentMember();

        User user =
                requireLinkedUser(
                        member
                );

        String avatarUrl =
                memberAvatarStorageService
                        .uploadMemberAvatar(
                                user.getId(),
                                file
                        );

        user.setAvatarUrl(
                avatarUrl
        );

        userRepository.save(
                user
        );

        memberTimelineRecorder.record(
                member.getId(),
                MemberTimelineType.MEMBER_PROFILE,
                "Cập nhật ảnh đại diện",
                "Hội viên đã thay đổi ảnh đại diện.",
                member.getId(),
                "MEMBER_AVATAR",
                "UPDATED",
                LocalDateTime.now()
        );

        return memberMapper.toResponse(
                member
        );
    }

    @Override
    @Transactional
    public void deleteMemberByAdmin(
            Long id
    ) {
        Member member =
                getActiveMemberById(
                        id
                );

        User user =
                requireLinkedUser(
                        member
                );

        member.setIsDeleted(true);
        member.setStatus(
                MemberStatus.INACTIVE
        );

        user.setIsDeleted(true);
        user.setStatus(
                UserStatus.INACTIVE
        );

        memberRepository.save(
                member
        );

        userRepository.save(
                user
        );

        memberTimelineRecorder.record(
                member.getId(),
                MemberTimelineType.SYSTEM,
                "Vô hiệu hóa hội viên",
                "Hồ sơ hội viên đã bị xóa mềm.",
                member.getId(),
                "MEMBER",
                "DELETED",
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void restoreMemberByAdmin(
            Long id
    ) {
        Member member =
                memberRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                ErrorCode.MEMBER_NOT_FOUND
                                        )
                        );

        User user =
                requireLinkedUser(
                        member
                );

        member.setIsDeleted(false);
        member.setStatus(
                MemberStatus.ACTIVE
        );

        user.setIsDeleted(false);
        user.setStatus(
                UserStatus.ACTIVE
        );

        memberRepository.save(
                member
        );

        userRepository.save(
                user
        );

        memberTimelineRecorder.record(
                member.getId(),
                MemberTimelineType.SYSTEM,
                "Khôi phục hội viên",
                "Hồ sơ hội viên đã được khôi phục.",
                member.getId(),
                "MEMBER",
                "RESTORED",
                LocalDateTime.now()
        );
    }

    private void updateUserInfoByAdmin(
            User user,
            MemberUpdateRequest request
    ) {
        if (request.getEmail() != null) {
            String newEmail =
                    normalizeEmail(
                            request.getEmail()
                    );

            String currentEmail =
                    user.getEmail() == null
                            ? null
                            : user.getEmail()
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            /*
             * Chỉ xử lý khi email thực sự thay đổi.
             */
            if (!Objects.equals(
                    currentEmail,
                    newEmail
            )) {
                validateUniqueEmail(
                        newEmail,
                        user.getId()
                );

                user.setEmail(
                        newEmail
                );

                /*
                 * QUAN TRỌNG:
                 *
                 * Email mới chưa được xác thực.
                 */
                user.setEmailVerified(
                        false
                );
            }
        }

        if (request.getFullName() != null) {
            user.setFullName(
                    normalizeRequired(
                            request.getFullName()
                    )
            );
        }

        if (request.getPhone() != null) {
            String phone =
                    normalizeNullable(
                            request.getPhone()
                    );

            validateUniquePhone(
                    phone,
                    user.getId()
            );

            user.setPhone(
                    phone
            );
        }
    }

    private void updateMemberInfoByAdmin(
            Member member,
            MemberUpdateRequest request
    ) {
        if (request.getGender() != null) {
            member.setGender(
                    request.getGender()
            );
        }

        if (request.getDateOfBirth() != null) {
            member.setDateOfBirth(
                    request.getDateOfBirth()
            );
        }

        if (request.getAddress() != null) {
            member.setAddress(
                    normalizeNullable(
                            request.getAddress()
                    )
            );
        }

        if (
                request.getEmergencyContactName()
                        != null
        ) {
            member.setEmergencyContactName(
                    normalizeNullable(
                            request.getEmergencyContactName()
                    )
            );
        }

        if (
                request.getEmergencyContactPhone()
                        != null
        ) {
            member.setEmergencyContactPhone(
                    normalizeNullable(
                            request.getEmergencyContactPhone()
                    )
            );
        }

        if (request.getFitnessGoal() != null) {
            member.setFitnessGoal(
                    request.getFitnessGoal()
            );
        }

        if (request.getHealthNote() != null) {
            member.setHealthNote(
                    normalizeNullable(
                            request.getHealthNote()
                    )
            );
        }
    }

    private void updateMyUserInfo(
            User user,
            MyMemberUpdateRequest request
    ) {
        if (
                request.getFullName() != null
        ) {
            user.setFullName(
                    normalizeRequired(
                            request.getFullName()
                    )
            );
        }

        if (
                request.getPhone() != null
        ) {
            String phone =
                    normalizeNullable(
                            request.getPhone()
                    );

            validateUniquePhone(
                    phone,
                    user.getId()
            );

            user.setPhone(
                    phone
            );
        }
    }

    private void updateMyMemberInfo(
            Member member,
            MyMemberUpdateRequest request
    ) {
        if (
                request.getGender() != null
        ) {
            member.setGender(
                    request.getGender()
            );
        }

        if (
                request.getDateOfBirth() != null
        ) {
            member.setDateOfBirth(
                    request.getDateOfBirth()
            );
        }

        if (
                request.getAddress() != null
        ) {
            member.setAddress(
                    normalizeNullable(
                            request.getAddress()
                    )
            );
        }

        if (
                request
                        .getEmergencyContactName()
                        != null
        ) {
            member.setEmergencyContactName(
                    normalizeNullable(
                            request
                                    .getEmergencyContactName()
                    )
            );
        }

        if (
                request
                        .getEmergencyContactPhone()
                        != null
        ) {
            member.setEmergencyContactPhone(
                    normalizeNullable(
                            request
                                    .getEmergencyContactPhone()
                    )
            );
        }

        if (
                request.getFitnessGoal() != null
        ) {
            member.setFitnessGoal(
                    request.getFitnessGoal()
            );
        }

        if (
                request.getHealthNote() != null
        ) {
            member.setHealthNote(
                    normalizeNullable(
                            request.getHealthNote()
                    )
            );
        }
    }

    private Member getActiveMemberById(
            Long id
    ) {
        if (id == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Member member =
                memberRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                ErrorCode.MEMBER_NOT_FOUND
                                        )
                        );

        if (
                Boolean.TRUE.equals(
                        member.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return member;
    }

    private User requireLinkedUser(
            Member member
    ) {
        if (
                member.getUser() == null
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NO_ACCOUNT
            );
        }

        return member.getUser();
    }

    private void validateUniqueUsername(
            String username,
            Long excludedUserId
    ) {
        userRepository
                .findByUsername(
                        username
                )
                .filter(
                        user ->
                                !Objects.equals(
                                        user.getId(),
                                        excludedUserId
                                )
                )
                .ifPresent(
                        user -> {
                            throw new AppException(
                                    ErrorCode.USERNAME_ALREADY_EXISTS
                            );
                        }
                );
    }

    private void validateUniqueEmail(
            String email,
            Long excludedUserId
    ) {
        userRepository
                .findByEmail(
                        email
                )
                .filter(
                        user ->
                                !Objects.equals(
                                        user.getId(),
                                        excludedUserId
                                )
                )
                .ifPresent(
                        user -> {
                            throw new AppException(
                                    ErrorCode.EMAIL_ALREADY_EXISTS
                            );
                        }
                );
    }

    private void validateUniquePhone(
            String phone,
            Long excludedUserId
    ) {
        if (
                phone == null
        ) {
            return;
        }

        userRepository
                .findByPhone(
                        phone
                )
                .filter(
                        user ->
                                !Objects.equals(
                                        user.getId(),
                                        excludedUserId
                                )
                )
                .ifPresent(
                        user -> {
                            throw new AppException(
                                    ErrorCode.PHONE_ALREADY_EXISTS
                            );
                        }
                );
    }

    private void syncUserStatusByMemberStatus(
            User user,
            MemberStatus memberStatus
    ) {
        switch (memberStatus) {

            case ACTIVE -> {
                user.setStatus(
                        UserStatus.ACTIVE
                );

                user.setIsDeleted(
                        false
                );
            }

            case INACTIVE,
                 SUSPENDED -> {
                user.setStatus(
                        UserStatus.INACTIVE
                );
            }
        }
    }

    private void validateDateOfBirth(
            LocalDate dateOfBirth
    ) {
        if (dateOfBirth == null) {
            return;
        }

        LocalDate today =
                LocalDate.now();

        /*
         * Ngày sinh bắt buộc phải trước hôm nay.
         */
        if (!dateOfBirth.isBefore(today)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * FitLife yêu cầu hội viên >= 10 tuổi.
         */
        LocalDate maximumDateOfBirth =
                today.minusYears(10);

        if (dateOfBirth.isAfter(
                maximumDateOfBirth
        )) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private String normalizeEmail(
            String value
    ) {
        return normalizeRequired(
                value
        ).toLowerCase(
                Locale.ROOT
        );
    }

    private String normalizeRequired(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return value.trim();
    }

    private String normalizeNullable(
            String value
    ) {
        if (
                value == null
        ) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String generateMemberCode() {
        String code;

        do {
            code =
                    "MEM"
                            + System
                            .currentTimeMillis();
        } while (
                memberRepository
                        .existsByMemberCode(
                                code
                        )
        );

        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public com.fitlife.trainer.dto.response.TrainerResponse getMyAssignedTrainer() {
        java.util.List<com.fitlife.trainer.dto.response.TrainerResponse> trainers = getMyAssignedTrainers();
        return trainers.isEmpty() ? null : trainers.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.fitlife.trainer.dto.response.TrainerResponse> getMyAssignedTrainers() {
        Member currentMember = currentMemberService.getCurrentMember();
        
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> assignmentsRaw = entityManager.createNativeQuery(
                "SELECT id, trainer_id, status FROM trainer_assignments WHERE member_id = :memberId AND status IN ('PENDING', 'ACTIVE', 'PENDING_CANCEL') ORDER BY id DESC")
                .setParameter("memberId", currentMember.getId())
                .getResultList();
                
        java.util.List<com.fitlife.trainer.dto.response.TrainerResponse> result = new java.util.ArrayList<>();
        for (Object[] row : assignmentsRaw) {
            Long assignmentId = ((Number) row[0]).longValue();
            Long trainerId = ((Number) row[1]).longValue();
            String status = (String) row[2];
            
            trainerRepository.findById(trainerId).ifPresent(t -> {
                com.fitlife.trainer.dto.response.TrainerResponse res = trainerMapper.toResponse(t);
                res.setAssignmentId(assignmentId);
                res.setAssignmentStatus(status);
                result.add(res);
            });
        }
        return result;
    }

    @Override
    @Transactional
    public void bookTrainer(Long trainerId) {
        Member currentMember = currentMemberService.getCurrentMember();
        
        com.fitlife.trainer.entity.Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new com.fitlife.common.exception.AppException(com.fitlife.common.exception.ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        if (Boolean.FALSE.equals(trainer.getIsAcceptingMembers())) {
            throw new com.fitlife.common.exception.AppException(
                    com.fitlife.common.exception.ErrorCode.INVALID_REQUEST,
                    "Huấn luyện viên hiện đã kích hoạt ngưng nhận học viên mới."
            );
        }

        // Kiểm tra xem hội viên đã có yêu cầu PENDING hoặc đang ACTIVE với chính HLV này chưa
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> existingThisTrainer = entityManager.createNativeQuery(
                "SELECT id, status FROM trainer_assignments WHERE member_id = :memberId AND trainer_id = :trainerId AND status IN ('PENDING', 'ACTIVE')")
                .setParameter("memberId", currentMember.getId())
                .setParameter("trainerId", trainer.getId())
                .getResultList();
                
        if (!existingThisTrainer.isEmpty()) {
            throw new com.fitlife.common.exception.AppException(
                    com.fitlife.common.exception.ErrorCode.INVALID_REQUEST,
                    "Bạn đã gửi yêu cầu hoặc đang đồng hành cùng Huấn luyện viên này rồi."
            );
        }

        entityManager.createNativeQuery(
                "INSERT INTO trainer_assignments (trainer_id, member_id, start_date, status) " +
                "VALUES (:trainerId, :memberId, :today, 'PENDING')")
                .setParameter("trainerId", trainer.getId())
                .setParameter("memberId", currentMember.getId())
                .setParameter("today", java.time.LocalDate.now())
                .executeUpdate();
                
        memberTimelineRecorder.record(
                currentMember.getId(),
                com.fitlife.member.timeline.enums.MemberTimelineType.MEMBER_PROFILE,
                "Gửi yêu cầu chọn Huấn luyện viên",
                "Đã gửi yêu cầu chọn Huấn luyện viên: " + trainer.getUser().getFullName() + " (Đang chờ HLV phê duyệt)",
                trainer.getId(),
                "TRAINER",
                "PENDING",
                java.time.LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void cancelTrainerBooking() {
        cancelTrainerBooking(null);
    }

    @Override
    @Transactional
    public void cancelTrainerBooking(Long trainerId) {
        Member currentMember = currentMemberService.getCurrentMember();
        
        String sql = "SELECT id, status, trainer_id FROM trainer_assignments WHERE member_id = :memberId AND status IN ('PENDING', 'ACTIVE', 'PENDING_CANCEL') ";
        if (trainerId != null) {
            sql += "AND trainer_id = :trainerId ";
        }
        sql += "ORDER BY id DESC";

        var query = entityManager.createNativeQuery(sql)
                .setParameter("memberId", currentMember.getId());
        if (trainerId != null) {
            query.setParameter("trainerId", trainerId);
        }

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> existing = query.getResultList();
                
        if (existing.isEmpty()) {
            throw new com.fitlife.common.exception.AppException(
                    com.fitlife.common.exception.ErrorCode.INVALID_REQUEST,
                    "Bạn hiện không có Huấn luyện viên nào để hủy."
            );
        }
        
        Object[] row = existing.get(0);
        Long assignmentId = ((Number) row[0]).longValue();
        String currentStatus = (String) row[1];
        
        if ("PENDING".equals(currentStatus)) {
            // Yêu cầu đang chờ duyệt: member tự hủy ngay được
            entityManager.createNativeQuery(
                    "UPDATE trainer_assignments SET status = 'CANCELLED', end_date = :today WHERE id = :id")
                    .setParameter("today", java.time.LocalDate.now())
                    .setParameter("id", assignmentId)
                    .executeUpdate();
                    
            memberTimelineRecorder.record(
                    currentMember.getId(),
                    com.fitlife.member.timeline.enums.MemberTimelineType.MEMBER_PROFILE,
                    "Hủy yêu cầu chọn Huấn luyện viên",
                    "Hội viên đã hủy yêu cầu chọn Huấn luyện viên thành công.",
                    currentMember.getId(),
                    "TRAINER",
                    "CANCELLED",
                    java.time.LocalDateTime.now()
            );
        } else if ("ACTIVE".equals(currentStatus)) {
            // Đã chọn thành công (ACTIVE): member gửi yêu cầu hủy và phải chờ Trainer xác nhận hủy
            entityManager.createNativeQuery(
                    "UPDATE trainer_assignments SET status = 'PENDING_CANCEL' WHERE id = :id")
                    .setParameter("id", assignmentId)
                    .executeUpdate();
                    
            memberTimelineRecorder.record(
                    currentMember.getId(),
                    com.fitlife.member.timeline.enums.MemberTimelineType.MEMBER_PROFILE,
                    "Gửi yêu cầu hủy Huấn luyện viên",
                    "Hội viên đã gửi yêu cầu dừng đồng hành (Đang chờ Huấn luyện viên xác nhận).",
                    currentMember.getId(),
                    "TRAINER",
                    "PENDING_CANCEL",
                    java.time.LocalDateTime.now()
            );
        } else if ("PENDING_CANCEL".equals(currentStatus)) {
            throw new com.fitlife.common.exception.AppException(
                    com.fitlife.common.exception.ErrorCode.INVALID_REQUEST,
                    "Yêu cầu hủy của bạn đã được gửi và đang chờ Huấn luyện viên xác nhận."
            );
        }
    }
}