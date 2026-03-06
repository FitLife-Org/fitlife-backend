package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// THÊM CHỮ NÀY: implements UserDetails
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

    // ====================================================================
    // CÁC HÀM BẮT BUỘC CỦA SPRING SECURITY (USERDETAILS INTERFACE)
    // ====================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security yêu cầu các quyền phải có tiền tố "ROLE_"
        // Ví dụ: ADMIN -> ROLE_ADMIN
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    // getPassword() và getUsername() đã được Lombok @Getter tự động sinh ra ở trên!

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không bao giờ hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Mật khẩu không hết hạn
    }

    @Override
    public boolean isEnabled() {
        // Chỉ cho phép đăng nhập nếu status là ACTIVE
        return "ACTIVE".equals(this.status);
    }
}