package com.fitlife.service;

import com.fitlife.dto.LoginRequest;
import com.fitlife.dto.LoginResponse;
import com.fitlife.dto.RegisterRequest;
import com.fitlife.entity.Member;
import com.fitlife.entity.User;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository; // Cần để tạo Profile 7 cột
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder; //

    // ====================================================================
    // 1. LOGIC ĐĂNG KÝ (Tạo User 5 cột & Member 7 cột)
    // ====================================================================
    @Transactional // Đảm bảo an toàn dữ liệu cho cả 2 bảng
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        // Tạo User (Đúng 5 cột: id, username, password, role, status)
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa BCrypt
                .role("MEMBER")
                .status("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);

        // Tạo Member (Đúng 7 cột: id, user_id, full_name, phone, email, status, avatar_url)
        Member member = Member.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .avatarUrl(null)
                .build();
        memberRepository.save(member);

        return "Đăng ký thành công!";
    }

    // ====================================================================
    // 2. LOGIC ĐĂNG NHẬP (Giữ nguyên của em)
    // ====================================================================
    public LoginResponse login(LoginRequest request) {
        // Kiểm tra Username/Password qua Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after auth!"));

        // In thẻ bài JWT
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}