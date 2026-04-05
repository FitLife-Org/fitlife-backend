package com.fitlife.identity.entity;

import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    @Column(name = "fit_coin")
    @Builder.Default
    private Integer fitCoin = 0;

    @Column(name = "reset_token", length = 10)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private Member member;

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