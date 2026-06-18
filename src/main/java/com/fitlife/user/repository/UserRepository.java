package com.fitlife.user.repository;

import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);
}