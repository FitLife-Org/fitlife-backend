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
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Logic Register: Create User + Member
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) //  BCrypt
                .role("MEMBER")
                .status("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);


        Member member = Member.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .avatarUrl(null)
                .build();
        memberRepository.save(member);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendWelcomeEmail(request.getEmail(), request.getFullName());
            System.out.println("Đã đẩy lệnh gửi email chào mừng vào luồng chạy ngầm cho: " + request.getEmail());
        }

        return "Đăng ký thành công!";
    }

    // Logic Login: Authenticate Spring Security + Generate JWT
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after auth!"));

        // Print cards JWT
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}