package com.fitlife.auth.repository;

import com.fitlife.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshToken r
            SET r.revoked = true,
                r.revokedAt = :revokedAt
            WHERE r.user.id = :userId
              AND r.revoked = false
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}