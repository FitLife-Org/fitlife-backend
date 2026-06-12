package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.*;
import com.fitlife.auth.repository.UserRepository;
import com.fitlife.auth.service.AuthService;
import com.fitlife.auth.service.OAuth2Service;
import com.fitlife.member.entity.Member;
import com.fitlife.auth.entity.User;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.common.mail.EmailService;
import com.fitlife.security.impl.JwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_MEMBER = "ROLE_MEMBER";

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OAuth2Service oAuth2Service;

    // Logic Register: Create User + Member
    @Transactional
    @Override
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username Ä‘Ă£ tá»“n táº¡i!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status("ACTIVE")
                .build();
        user.setRole(ROLE_MEMBER);
        User savedUser = userRepository.save(user);
        userRepository.assignRoleToUser(savedUser.getId(), ROLE_MEMBER);

        Member member = Member.builder()
                .user(savedUser)
                .memberCode("MEM" + String.format("%06d", savedUser.getId()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .avatarUrl(null)
                .build();
        memberRepository.save(member);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendWelcomeEmail(request.getEmail(), request.getFullName());
            System.out.println("ÄĂ£ Ä‘áº©y lá»‡nh gá»­i email chĂ o má»«ng vĂ o luá»“ng cháº¡y ngáº§m cho: " + request.getEmail());
        }

        return "ÄÄƒng kĂ½ thĂ nh cĂ´ng!";
    }

    // Logic Login: Authenticate Spring Security + Generate JWT
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after auth!"));

        // Print cards JWT
        String token = jwtServiceImpl.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

// FUNCTION FORGOT PASSWORD
    // Create code OTP random 6 numbers
    private String generateOtp() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }

    // 1. Stream forgot password (Create OTP and send mail)
    @Transactional
    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y tĂ i khoáº£n vá»›i email nĂ y!"));

        User user = member.getUser();

        String otp = generateOtp();
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(request.getEmail(), otp);

        return "MĂ£ OTP Ä‘Ă£ Ä‘Æ°á»£c gá»­i Ä‘áº¿n email cá»§a báº¡n. Vui lĂ²ng kiá»ƒm tra há»™p thÆ°!";
    }

    // 2. Stream reset password (Check OTP and reset new password)
    @Transactional
    @Override
    public String resetPassword(ResetPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y tĂ i khoáº£n vá»›i email nĂ y!"));

        User user = member.getUser();

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp())) {
            throw new RuntimeException("MĂ£ OTP khĂ´ng chĂ­nh xĂ¡c!");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("MĂ£ OTP Ä‘Ă£ háº¿t háº¡n! Vui lĂ²ng yĂªu cáº§u gá»­i láº¡i.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "KhĂ´i phá»¥c máº­t kháº©u thĂ nh cĂ´ng! Báº¡n cĂ³ thá»ƒ Ä‘Äƒng nháº­p báº±ng máº­t kháº©u má»›i.";
    }

    //  Register by Google (OAUTH2)

    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";


}