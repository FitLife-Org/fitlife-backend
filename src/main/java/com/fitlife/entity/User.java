package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entity represents a system user
 * Implements UserDetails for Spring Security integration
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // "ADMIN", "MEMBER", "STAFF"

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "ACTIVE", "INACTIVE"

    // Functions to implement UserDetails interface for Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security requires roles to be prefixed with "ROLE_". Example: "ADMIN" -> "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    // getPassword() and getUsername() are already provided by Lombok's @Getter

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account is not expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password is expired
    }

    @Override
    public boolean isEnabled() {
        // User is enabled if status is "ACTIVE"
        return "ACTIVE".equals(this.status);
    }
}