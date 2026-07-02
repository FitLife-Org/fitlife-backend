package com.fitlife.user.repository;

import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String email, String username);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN u.roles r
            WHERE
                (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:roleCode IS NULL OR :roleCode = '' OR r.code = :roleCode)
                AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> searchAdminUsers(
            @Param("keyword") String keyword,
            @Param("roleCode") String roleCode,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}