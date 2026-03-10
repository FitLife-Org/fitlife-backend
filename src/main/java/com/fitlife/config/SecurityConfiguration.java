package com.fitlife.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC (Mở khóa toàn bộ cho khách)
                        .requestMatchers("/auth/**", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/ai/**", "/api/v1/ai/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Cho phép ai cũng xem được danh sách gói tập
                        .requestMatchers(HttpMethod.GET, "/packages/**", "/api/v1/packages/**").permitAll()

                        .requestMatchers("/payment/vnpay-return", "/api/v1/payment/vnpay-return", "/payment/vnpay-ipn", "/api/v1/payment/vnpay-ipn").permitAll()

                        // 2. BUSINESS & HEALTH (Chỉ cần đăng nhập là dùng được)
                        .requestMatchers("/workout/**", "/api/v1/workout/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_STAFF", "ROLE_ADMIN", "MEMBER", "STAFF", "ADMIN")
                        .requestMatchers("/health/**", "/api/v1/health/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_STAFF", "ROLE_ADMIN", "MEMBER", "STAFF", "ADMIN")
                        .requestMatchers("/members/**", "/api/v1/members/**").hasAnyAuthority("ROLE_MEMBER", "ROLE_STAFF", "ROLE_ADMIN", "MEMBER", "STAFF", "ADMIN")

                        .requestMatchers("/payment/**", "/api/v1/payment/**").hasAnyAuthority("ROLE_MEMBER", "MEMBER")

                        // 3. ADMINISTRATION (Chỉ Admin/Staff mới được đụng vào)
                        .requestMatchers(HttpMethod.POST, "/packages/**", "/api/v1/packages/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ADMIN", "STAFF")
                        .requestMatchers("/checkin/**", "/api/v1/checkin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_MEMBER", "ADMIN", "STAFF", "MEMBER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}