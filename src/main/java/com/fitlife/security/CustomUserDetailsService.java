package com.fitlife.security;

import com.fitlife.common.exception.ErrorCode;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email or username: " + identifier
                ));

        validateUserCanLogin(user);

        return new CustomUserDetails(user);
    }

    private void validateUserCanLogin(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new DisabledException(ErrorCode.ACCOUNT_DELETED.name());
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException(ErrorCode.ACCOUNT_LOCKED.name());
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException(ErrorCode.ACCOUNT_INACTIVE.name());
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException(ErrorCode.ACCOUNT_INACTIVE.name());
        }
    }
}