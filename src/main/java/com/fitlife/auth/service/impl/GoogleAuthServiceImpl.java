package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.LoginResponse;
import com.fitlife.auth.entity.User;
import com.fitlife.auth.repository.UserRepository;
import com.fitlife.auth.service.OAuth2Service;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.security.impl.JwtServiceImpl;
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

    private static final String ROLE_MEMBER = "ROLE_MEMBER";

    // TiĂªm cĂ¡c bean cáº§n thiáº¿t vĂ o
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public LoginResponse googleLogin(String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // Tech Lead Note: Thá»­ dĂ¹ng cáº£ Access Token hoáº·c ID Token vá»›i endpoint nĂ y
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Map<String, Object> payload;
        try {
            // Gá»i Google Ä‘á»ƒ láº¥y thĂ´ng tin user
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            payload = response.getBody();
        } catch (Exception e) {
            // In ra lá»—i thá»±c táº¿ á»Ÿ Console Ä‘á»ƒ em debug dá»… hÆ¡n
            System.err.println("Lá»—i gá»i Google API: " + e.getMessage());
            throw new RuntimeException("XĂ¡c thá»±c Google tháº¥t báº¡i: Token khĂ´ng há»£p lá»‡ hoáº·c Ä‘Ă£ háº¿t háº¡n!");
        }

        if (payload == null || !payload.containsKey("email")) {
            throw new RuntimeException("KhĂ´ng láº¥y Ä‘Æ°á»£c dá»¯ liá»‡u tá»« Google!");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 2. TĂ¬m user theo email (username)
        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            // 3. Tá»± Ä‘á»™ng Ä‘Äƒng kĂ½ náº¿u chÆ°a cĂ³ tĂ i khoáº£n
            user = User.builder()
                    .username(email)
                    .email(email)
                    .fullName(name)
                    .avatarUrl(picture)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .status("ACTIVE")
                    .build();
            user.setRole(ROLE_MEMBER);
            user = userRepository.save(user);
            userRepository.assignRoleToUser(user.getId(), ROLE_MEMBER);

            Member member = Member.builder()
                    .user(user)
                    .memberCode("MEM" + String.format("%06d", user.getId()))
                    .fullName(name)
                    .email(email)
                    .avatarUrl(picture)
                    .phone("ChÆ°a cáº­p nháº­t")
                    .status("ACTIVE")
                    .build();
            memberRepository.save(member);
        }

        // 4. Táº¡o JWT cá»§a riĂªng há»‡ thá»‘ng FitLife tráº£ vá» cho ngÆ°á»i dĂ¹ng
        String jwtToken = jwtServiceImpl.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}