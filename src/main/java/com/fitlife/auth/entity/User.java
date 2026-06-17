package com.fitlife.auth.entity;

import com.fitlife.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    private static final String DEFAULT_ROLE = "ROLE_MEMBER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "auth_provider", nullable = false, length = 50)
    private String authProvider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "email_verified",  nullable = false)
    private Boolean emailVerified;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new LinkedHashSet<>();

    @Transient
    private String pendingRole;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private Member member;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority(getRole()));
        }
        return roles.stream()
                .map(Role::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            return roles.stream()
                    .map(Role::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .findFirst()
                    .orElse(DEFAULT_ROLE);
        }
        if (pendingRole != null && !pendingRole.isBlank()) {
            return pendingRole;
        }
        return DEFAULT_ROLE;
    }

    public void setRole(String role) {
        this.pendingRole = role;
    }

    public String getRawRoleRequest() {
        return pendingRole;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(this.status) && !Boolean.TRUE.equals(this.isDeleted);
    }
}