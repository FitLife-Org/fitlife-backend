package com.fitlife.user.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.user.dto.request.AdminCreateInternalUserRequest;
import com.fitlife.user.dto.request.AdminUpdateUserRequest;
import com.fitlife.user.dto.request.AdminUpdateUserRolesRequest;
import com.fitlife.user.dto.request.AdminUpdateUserStatusRequest;
import com.fitlife.user.dto.request.AdminUserSearchRequest;
import com.fitlife.user.dto.request.ChangePasswordRequest;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.UserProfileResponse;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.mapper.UserMapper;
import com.fitlife.user.repository.RoleRepository;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.user.service.UserService;
import com.fitlife.user.dto.request.UpdateUserProfileRequest;
import com.fitlife.member.avatar.service.MemberAvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl
        implements UserService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private static final Set<String>
            ALLOWED_INTERNAL_ROLES = Set.of(
            "ROLE_ADMIN",
            "ROLE_STAFF",
            "ROLE_TRAINER"
    );

    private static final Set<String>
            ALLOWED_USER_ROLES = Set.of(
            "ROLE_ADMIN",
            "ROLE_STAFF",
            "ROLE_TRAINER",
            "ROLE_MEMBER"
    );

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberAvatarStorageService memberAvatarStorageService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {
        User currentUser =
                getCurrentAuthenticatedUser();

        return userMapper.toUserProfileResponse(
                currentUser
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse>
    getAdminUsers(
            AdminUserSearchRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int page =
                normalizePage(
                        request.getPage()
                );

        int size =
                normalizeSize(
                        request.getSize()
                );

        UserStatus status =
                parseUserStatus(
                        request.getStatus()
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<User> userPage =
                userRepository.searchAdminUsers(
                        normalizeKeyword(
                                request.getKeyword()
                        ),
                        normalizeRoleCode(
                                request.getRoleCode()
                        ),
                        status,
                        pageable
                );

        return PageResponse.from(
                userPage,
                userMapper::toAdminUserResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse
    getAdminUserDetail(
            Long id
    ) {
        User user =
                getUserById(id);

        return userMapper
                .toAdminUserDetailResponse(
                        user
                );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse
    createInternalUser(
            AdminCreateInternalUserRequest request
    ) {
        validateCreateRequest(request);

        String roleCode =
                normalizeRequiredRoleCode(
                        request.getRoleCode()
                );

        if (!ALLOWED_INTERNAL_ROLES
                .contains(roleCode)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Role role =
                roleRepository.findByCode(roleCode)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.ROLE_NOT_FOUND
                                )
                        );

        UserStatus status =
                parseUserStatusWithDefault(
                        request.getStatus()
                );

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "123456";
        }

        User user =
                User.builder()
                        .username(
                                normalizeRequiredText(
                                        request.getUsername()
                                )
                        )
                        .email(
                                normalizeEmail(
                                        request.getEmail()
                                )
                        )
                        .passwordHash(
                                passwordEncoder.encode(
                                        rawPassword
                                )
                        )
                        .fullName(
                                normalizeRequiredText(
                                        request.getFullName()
                                )
                        )
                        .phone(
                                normalizeRequiredText(
                                        request.getPhone()
                                )
                        )
                        .status(status)
                        .authProvider(
                                AuthProvider.LOCAL
                        )
                        .emailVerified(true)
                        .isDeleted(false)
                        .roles(
                                new HashSet<>(
                                        Set.of(role)
                                )
                        )
                        .build();

        User savedUser =
                userRepository.save(user);

        return userMapper
                .toAdminUserDetailResponse(
                        savedUser
                );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUser(
            Long id,
            AdminUpdateUserRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        User user =
                getUserById(id);

        validateSelfStatusUpdate(
                user,
                request.getStatus()
        );

        updateUsernameIfPresent(
                user,
                request.getUsername()
        );

        updateEmailIfPresent(
                user,
                request.getEmail()
        );

        updateFullNameIfPresent(
                user,
                request.getFullName()
        );

        updatePhoneIfPresent(
                user,
                request.getPhone()
        );

        updateStatusIfPresent(
                user,
                request.getStatus()
        );

        User savedUser =
                userRepository.save(user);

        return userMapper
                .toAdminUserDetailResponse(
                        savedUser
                );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUserStatus(
            Long id,
            AdminUpdateUserStatusRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        User user =
                getUserById(id);

        validateNotCurrentUser(user);

        UserStatus newStatus =
                parseRequiredUserStatus(
                        request.getStatus()
                );

        user.setStatus(newStatus);

        User savedUser =
                userRepository.save(user);

        return userMapper
                .toAdminUserDetailResponse(
                        savedUser
                );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUserRoles(
            Long id,
            AdminUpdateUserRolesRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        User user =
                getUserById(id);

        validateNotCurrentUser(user);

        Set<String> normalizedRoleCodes =
                normalizeRoleCodes(
                        request.getRoleCodes()
                );

        validateRoleCodesAllowed(
                normalizedRoleCodes
        );

        List<Role> roles =
                roleRepository.findByCodeIn(
                        normalizedRoleCodes
                );

        if (roles.size()
                != normalizedRoleCodes.size()) {
            throw new AppException(
                    ErrorCode.ROLE_NOT_FOUND
            );
        }

        user.setRoles(
                new HashSet<>(roles)
        );

        User savedUser =
                userRepository.save(user);

        return userMapper
                .toAdminUserDetailResponse(
                        savedUser
                );
    }

    @Override
    @Transactional
    public void changePassword(
            ChangePasswordRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        User currentUser =
                getCurrentAuthenticatedUser();

        validateCurrentPassword(
                request.getCurrentPassword(),
                currentUser
        );

        validatePasswordConfirmation(
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        validateNewPasswordDifferent(
                request.getNewPassword(),
                currentUser
        );

        currentUser.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(currentUser);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication
                .isAuthenticated()) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof
                CustomUserDetails customUserDetails)) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        Long userId =
                customUserDetails.getId();

        return getUserById(userId);
    }

    private User getUserById(
            Long id
    ) {
        if (id == null || id <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private void validateCreateRequest(
            AdminCreateInternalUserRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String username =
                normalizeRequiredText(
                        request.getUsername()
                );

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        String phone =
                normalizeRequiredText(
                        request.getPhone()
                );

        if (userRepository
                .existsByUsername(username)) {
            throw new AppException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }

        if (userRepository
                .existsByEmail(email)) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        if (userRepository
                .existsByPhone(phone)) {
            throw new AppException(
                    ErrorCode.PHONE_ALREADY_EXISTS
            );
        }
    }

    private void validateSelfStatusUpdate(
            User targetUser,
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return;
        }

        User currentUser =
                getCurrentAuthenticatedUser();

        if (currentUser.getId()
                .equals(targetUser.getId())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateNotCurrentUser(
            User targetUser
    ) {
        User currentUser =
                getCurrentAuthenticatedUser();

        if (currentUser.getId()
                .equals(targetUser.getId())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateCurrentPassword(
            String currentPassword,
            User currentUser
    ) {
        if (!passwordEncoder.matches(
                currentPassword,
                currentUser.getPasswordHash()
        )) {
            throw new AppException(
                    ErrorCode
                            .CURRENT_PASSWORD_INCORRECT
            );
        }
    }

    private void validatePasswordConfirmation(
            String newPassword,
            String confirmPassword
    ) {
        if (newPassword == null
                || !newPassword.equals(
                confirmPassword
        )) {
            throw new AppException(
                    ErrorCode
                            .PASSWORD_CONFIRM_NOT_MATCH
            );
        }
    }

    private void validateNewPasswordDifferent(
            String newPassword,
            User currentUser
    ) {
        if (passwordEncoder.matches(
                newPassword,
                currentUser.getPasswordHash()
        )) {
            throw new AppException(
                    ErrorCode
                            .NEW_PASSWORD_SAME_AS_OLD
            );
        }
    }

    private void updateUsernameIfPresent(
            User user,
            String username
    ) {
        String normalizedUsername =
                normalizeOptionalText(username);

        if (normalizedUsername == null
                || normalizedUsername.equals(
                user.getUsername()
        )) {
            return;
        }

        if (userRepository.existsByUsername(
                normalizedUsername
        )) {
            throw new AppException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }

        user.setUsername(
                normalizedUsername
        );
    }

    private void updateEmailIfPresent(
            User user,
            String email
    ) {
        String normalizedEmail =
                normalizeOptionalEmail(email);

        if (normalizedEmail == null
                || normalizedEmail.equals(
                user.getEmail()
        )) {
            return;
        }

        if (userRepository.existsByEmail(
                normalizedEmail
        )) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        user.setEmail(normalizedEmail);
    }

    private void updateFullNameIfPresent(
            User user,
            String fullName
    ) {
        String normalizedFullName =
                normalizeOptionalText(fullName);

        if (normalizedFullName == null) {
            return;
        }

        user.setFullName(
                normalizedFullName
        );
    }

    private void updatePhoneIfPresent(
            User user,
            String phone
    ) {
        String normalizedPhone =
                normalizeOptionalText(phone);

        if (normalizedPhone == null
                || normalizedPhone.equals(
                user.getPhone()
        )) {
            return;
        }

        if (userRepository.existsByPhone(
                normalizedPhone
        )) {
            throw new AppException(
                    ErrorCode.PHONE_ALREADY_EXISTS
            );
        }

        user.setPhone(normalizedPhone);
    }

    private void updateStatusIfPresent(
            User user,
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return;
        }

        user.setStatus(
                parseRequiredUserStatus(status)
        );
    }

    private Set<String> normalizeRoleCodes(
            Set<String> roleCodes
    ) {
        if (roleCodes == null
                || roleCodes.isEmpty()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Set<String> normalized =
                roleCodes.stream()
                        .filter(roleCode ->
                                roleCode != null
                                        && !roleCode.isBlank()
                        )
                        .map(roleCode ->
                                roleCode.trim()
                                        .toUpperCase()
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (normalized.isEmpty()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private void validateRoleCodesAllowed(
            Set<String> roleCodes
    ) {
        boolean hasInvalidRole =
                roleCodes.stream()
                        .anyMatch(roleCode ->
                                !ALLOWED_USER_ROLES
                                        .contains(roleCode)
                        );

        if (hasInvalidRole) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private UserStatus parseUserStatus(
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return null;
        }

        return parseRequiredUserStatus(
                status
        );
    }

    private UserStatus parseUserStatusWithDefault(
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return UserStatus.ACTIVE;
        }

        return parseRequiredUserStatus(
                status
        );
    }

    private UserStatus parseRequiredUserStatus(
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        try {
            return UserStatus.valueOf(
                    status.trim()
                            .toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private String normalizeRoleCode(
            String roleCode
    ) {
        if (roleCode == null
                || roleCode.isBlank()) {
            return null;
        }

        return roleCode.trim()
                .toUpperCase();
    }

    private String normalizeRequiredRoleCode(
            String roleCode
    ) {
        String normalized =
                normalizeRoleCode(roleCode);

        if (normalized == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String normalizeKeyword(
            String keyword
    ) {
        return normalizeOptionalText(
                keyword
        );
    }

    private String normalizeRequiredText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(
            String email
    ) {
        return normalizeRequiredText(email)
                .toLowerCase();
    }

    private String normalizeOptionalEmail(
            String email
    ) {
        String normalized =
                normalizeOptionalText(email);

        return normalized == null
                ? null
                : normalized.toLowerCase();
    }

    private int normalizePage(
            int page
    ) {
        return Math.max(
                page,
                DEFAULT_PAGE
        );
    }

    private int normalizeSize(
            int size
    ) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(
                size,
                MAX_SIZE
        );
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        
        String normalizedFullName = normalizeRequiredText(request.getFullName());
        String normalizedPhone = normalizeRequiredText(request.getPhone());
        
        if (!normalizedPhone.equals(currentUser.getPhone())) {
            if (userRepository.existsByPhone(normalizedPhone)) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
        }
        
        currentUser.setFullName(normalizedFullName);
        currentUser.setPhone(normalizedPhone);
        
        return userMapper.toUserProfileResponse(userRepository.save(currentUser));
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyAvatar(org.springframework.web.multipart.MultipartFile file) {
        User currentUser = getCurrentAuthenticatedUser();
        String avatarUrl = memberAvatarStorageService.uploadMemberAvatar(currentUser.getId(), file);
        currentUser.setAvatarUrl(avatarUrl);
        return userMapper.toUserProfileResponse(userRepository.save(currentUser));
    }
}