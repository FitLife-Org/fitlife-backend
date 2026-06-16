package com.fitlife.auth.repository;

import com.fitlife.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    @Modifying
    @Query(value = "INSERT IGNORE INTO user_roles (user_id, role_id) " +
            "SELECT :userId, r.id FROM roles r WHERE r.code = :roleCode", nativeQuery = true)
    void assignRoleToUser(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}