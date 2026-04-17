package com.fitlife.identity.service.impl;

import com.fitlife.identity.dto.*;
import com.fitlife.identity.repository.UserRepository;
import com.fitlife.identity.service.AuthService;
import com.fitlife.member.entity.Member;
import com.fitlife.identity.entity.User;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.core.mail.EmailService;
import com.fitlife.core.security.impl.JwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Logic Register: Create User + Member
    @Transactional
    @Override
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        User user = member.getUser();

        String otp = generateOtp();
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(request.getEmail(), otp);

        return "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư!";
    }

    // 2. Stream reset password (Check OTP and reset new password)
    @Transactional
    @Override
    public String resetPassword(ResetPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        User user = member.getUser();

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác!");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn! Vui lòng yêu cầu gửi lại.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Khôi phục mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.";
    }

    //  Register by Google (OAUTH2)

    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";

    @Transactional
    @Override
    public LoginResponse googleLogin(String token) {
        // 1. Bring Token to the Google server to ask for User information
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn!");
        }

        Map<String, Object> payload = response.getBody();
        if (payload == null || !payload.containsKey("email")) {
            throw new RuntimeException("Không lấy được email từ Google");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 2. Check Email have in the Database or not
        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            // 3. If don't have one -> Automatically register a new account for guests
            user = User.builder()
                    .username(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("ROLE_MEMBER")
                    .status("ACTIVE")
                    .build();
            user = userRepository.save(user);

            Member member = Member.builder()
                    .user(user)
                    .fullName(name)
                    .email(email)
                    .avatarUrl(picture)
                    .status("ACTIVE")
                    .build();
            memberRepository.save(member);
        }

        // 4. If you already have (or just created) -> Give them your system's JWT Token
        String jwtToken = jwtServiceImpl.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}