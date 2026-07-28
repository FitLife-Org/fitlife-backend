package com.fitlife.security.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.security.service.CurrentUserService;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        validateAuthentication(authentication);

        User user = resolveUser(authentication);
        validateCurrentUser(user);

        return user;
    }

    private User resolveUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        /*
         * Luồng JWT chuẩn của FitLife đặt CustomUserDetails vào SecurityContext.
         * Nhánh này không query DB lần nữa và không phụ thuộc principal là email
         * hay username.
         */
        if (principal instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();

            if (user == null || user.getId() == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return user;
        }

        /*
         * Fallback cho các Authentication provider khác hoặc test security.
         */
        String identifier = authentication.getName();

        if (identifier == null || identifier.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String normalizedIdentifier = identifier.trim().toLowerCase();

        return userRepository
                .findByUsernameOrEmail(
                        normalizedIdentifier,
                        normalizedIdentifier
                )
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND)
                );
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private void validateCurrentUser(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AppException(ErrorCode.ACCOUNT_DELETED);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
    }
}
