package com.fitlife.auth.repository;

import com.fitlife.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<
        EmailVerificationToken,
        Long
        > {

    Optional<EmailVerificationToken>
    findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
            DELETE FROM EmailVerificationToken token
            WHERE token.user.id = :userId
              AND token.used = false
            """)
    void deleteAllByUserIdAndUsedFalse(
            @Param("userId")
            Long userId
    );
}