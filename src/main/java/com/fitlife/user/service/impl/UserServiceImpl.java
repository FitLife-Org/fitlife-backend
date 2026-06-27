package com.fitlife.user.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.user.dto.request.AdminUserSearchRequest;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.PageResponse;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.mapper.UserMapper;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public AdminUserResponse getCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return userMapper.toAdminUserResponse(currentUser);
    }

    @Override
    public PageResponse<AdminUserResponse> getAdminUsers(AdminUserSearchRequest request) {
        int page = normalizePage(request.getPage());
        int size = normalizeSize(request.getSize());

        UserStatus status = parseUserStatus(request.getStatus());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> userPage = userRepository.searchAdminUsers(
                normalizeKeyword(request.getKeyword()),
                normalizeRoleCode(request.getRoleCode()),
                status,
                pageable
        );

        List<AdminUserResponse> content = userPage.getContent()
                .stream()
                .map(userMapper::toAdminUserResponse)
                .toList();

        return PageResponse.<AdminUserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    public AdminUserDetailResponse getAdminUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toAdminUserDetailResponse(user);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            Long userId = customUserDetails.getId();

            return userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }

        return roleCode.trim().toUpperCase();
    }

    private UserStatus parseUserStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}