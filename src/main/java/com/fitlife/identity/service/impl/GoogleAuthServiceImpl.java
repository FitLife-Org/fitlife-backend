package com.fitlife.identity.service.impl;

import com.fitlife.identity.dto.LoginResponse;
import com.fitlife.identity.entity.User;
import com.fitlife.identity.repository.UserRepository;
import com.fitlife.identity.service.OAuth2Service;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.core.security.impl.JwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements OAuth2Service {

    // Tiêm các bean cần thiết vào
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public LoginResponse googleLogin(String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // Tech Lead Note: Thử dùng cả Access Token hoặc ID Token với endpoint này
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Map<String, Object> payload;
        try {
            // Gọi Google để lấy thông tin user
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            payload = response.getBody();
        } catch (Exception e) {
            // In ra lỗi thực tế ở Console để em debug dễ hơn
            System.err.println("Lỗi gọi Google API: " + e.getMessage());
            throw new RuntimeException("Xác thực Google thất bại: Token không hợp lệ hoặc đã hết hạn!");
        }

        if (payload == null || !payload.containsKey("email")) {
            throw new RuntimeException("Không lấy được dữ liệu từ Google!");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 2. Tìm user theo email (username)
        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            // 3. Tự động đăng ký nếu chưa có tài khoản
            user = User.builder()
                    .username(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("MEMBER") // ĐỒNG NHẤT: Dùng "MEMBER", đừng dùng "ROLE_MEMBER" ở đây
                    .status("ACTIVE")
                    .build();
            user = userRepository.save(user);

            Member member = Member.builder()
                    .user(user)
                    .fullName(name)
                    .email(email)
                    .avatarUrl(picture)
                    .phone("Chưa cập nhật")
                    .status("ACTIVE")
                    .build();
            memberRepository.save(member);
        }

        // 4. Tạo JWT của riêng hệ thống FitLife trả về cho người dùng
        String jwtToken = jwtServiceImpl.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}