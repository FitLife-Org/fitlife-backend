package com.fitlife.security;

import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null) {
            return Collections.emptyList();
        }

        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /**
     * Principal của hệ thống hiện dùng email.
     *
     * CustomUserDetailsService cần hỗ trợ đăng nhập bằng cả email
     * và username, nhưng sau khi xác thực thành công principal
     * sẽ lấy email làm username.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE
                && Boolean.TRUE.equals(user.getEmailVerified())
                && Boolean.FALSE.equals(user.getIsDeleted());
    }
}