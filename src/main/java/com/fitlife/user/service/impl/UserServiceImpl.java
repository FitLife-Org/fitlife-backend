package com.fitlife.user.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.role.entity.Role;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.user.dto.response.UserResponse;
import com.fitlife.user.entity.User;
import com.fitlife.user.mapper.UserMapper;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return userMapper.toUserResponse(currentUser);
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
}