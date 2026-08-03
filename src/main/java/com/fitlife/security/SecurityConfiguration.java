package com.fitlife.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final AuthenticationProvider
            authenticationProvider;

    private final RestAuthenticationEntryPoint
            authenticationEntryPoint;

    private final RestAccessDeniedHandler
            accessDeniedHandler;

    private final CorsConfigurationSource
            corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // CORS PREFLIGHT
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =====================================================
                        // COMMON PUBLIC
                        // =====================================================

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/uploads/**"
                        ).permitAll()

                        /*
                         * Swagger hiện giữ public để phục vụ local/demo.
                         * Sang Foundation 04 sẽ chuyển theo profile.
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // =====================================================
                        // AUTH PUBLIC
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/google-login",
                                "/auth/refresh-token",
                                "/auth/resend-verification-email",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/verify-email"
                        ).permitAll()

                        /*
                         * Logout cần người dùng đã đăng nhập.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/logout",
                                "/auth/logout-all"
                        ).authenticated()

                        // =====================================================
                        // VNPAY PUBLIC CALLBACK
                        // =====================================================

                        /*
                         * Callback được public nhưng service bắt buộc phải:
                         * - kiểm tra checksum;
                         * - kiểm tra amount;
                         * - xử lý idempotent.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/payments/vnpay/return",
                                "/payments/vnpay/ipn"
                        ).permitAll()

                        // =====================================================
                        // PUBLIC PACKAGE CATALOG
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/gym-packages",
                                "/gym-packages/**",
                                "/package-durations",
                                "/package-durations/**"
                        ).permitAll()

                        // =====================================================
                        // PUBLIC WEBSITE
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/public/home",
                                "/public/packages",
                                "/public/packages/**",
                                "/public/trainers",
                                "/public/trainers/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/public/contact-requests"
                        ).permitAll()

                        // =====================================================
                        // ADMIN — MUST COME BEFORE GENERIC ROUTES
                        // =====================================================

                        .requestMatchers(
                                "/admin/equipment/**"
                        ).hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // =====================================================
                        // STAFF
                        // =====================================================

                        .requestMatchers(
                                "/staff/**"
                        ).hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )

                        // =====================================================
                        // TRAINER
                        // =====================================================

                        /*
                         * Các API nghiệp vụ trainer.
                         * Admin có thể được phép hỗ trợ kiểm tra khi cần.
                         */
                        .requestMatchers(
                                "/trainer/**"
                        ).hasAnyRole(
                                "TRAINER",
                                "ADMIN"
                        )

                        /*
                         * Hồ sơ trainer hiện tại:
                         * PUT /trainers/me
                         * GET /trainers/me
                         */
                        .requestMatchers(
                                "/trainers/me/**",
                                "/trainers/me"
                        ).hasRole("TRAINER")

                        /*
                         * Danh sách trainer hiện tại có GET /trainers.
                         * Cho phép authenticated để Member có thể xem.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/trainers",
                                "/trainers/**"
                        ).authenticated()

                        // =====================================================
                        // MEMBER SELF-SERVICE
                        // =====================================================

                        .requestMatchers(
                                "/members/me",
                                "/members/me/**"
                        ).hasRole("MEMBER")

                        .requestMatchers(
                                "/body-metrics/me",
                                "/body-metrics/me/**"
                        ).hasRole("MEMBER")

                        .requestMatchers(
                                "/ai/suggestions",
                                "/ai/suggestions/**"
                        ).hasRole("MEMBER")

                        /*
                         * Workout self-service.
                         *
                         * Trainer route bắt đầu bằng /trainer/**
                         * Admin route bắt đầu bằng /admin/**
                         * nên đã được match phía trên.
                         */
                        .requestMatchers(
                                "/workout-plans",
                                "/workout-plans/**"
                        ).hasRole("MEMBER")

                        /*
                         * Nutrition self-service.
                         */
                        .requestMatchers(
                                "/nutrition-plans",
                                "/nutrition-plans/**"
                        ).hasRole("MEMBER")

                        // =====================================================
                        // MEMBER SUBSCRIPTION / PAYMENT / INVOICE
                        // =====================================================

                        .requestMatchers(
                                "/subscriptions",
                                "/subscriptions/**"
                        ).hasRole("MEMBER")

                        /*
                         * Callback VNPay đã được permit phía trên.
                         * Các route payment còn lại dành cho Member.
                         */
                        .requestMatchers(
                                "/payments",
                                "/payments/**"
                        ).hasRole("MEMBER")

                        .requestMatchers(
                                "/invoices",
                                "/invoices/**"
                        ).hasRole("MEMBER")

                        // =====================================================
                        // CHECK-IN
                        // =====================================================

                        /*
                         * Member quét QR phòng gym.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/check-ins/scan-gym-qr"
                        ).hasRole("MEMBER")

                        .requestMatchers(
                                "/check-ins/me",
                                "/check-ins/me/**"
                        ).hasRole("MEMBER")

                        /*
                         * Route cũ/member flow hiện tại.
                         */
                        .requestMatchers(
                                "/member/check-ins/**",
                                "/member/check-outs/**"
                        ).hasRole("MEMBER")

                        /*
                         * Các route /check-ins còn lại là vận hành.
                         */
                        .requestMatchers(
                                "/check-ins",
                                "/check-ins/**"
                        ).hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )

                        // =====================================================
                        // USER PROFILE
                        // =====================================================

                        .requestMatchers(
                                "/users/me",
                                "/users/me/**"
                        ).authenticated()

                        // =====================================================
                        // EQUIPMENT
                        // =====================================================

                        /*
                         * Danh sách thiết bị cho user đã đăng nhập.
                         * CRUD admin đã được chặn bởi /admin/** phía trên.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/equipment",
                                "/equipment/**"
                        ).authenticated()

                        // =====================================================
                        // TEST ENDPOINTS
                        // =====================================================

                        /*
                         * Không public /test/**.
                         *
                         * Nếu còn endpoint test trong source, chỉ Admin mới gọi
                         * được. Foundation 04 sẽ tách hẳn theo Spring profile.
                         */
                        .requestMatchers(
                                "/test/**"
                        ).hasRole("ADMIN")

                        // =====================================================
                        // FALLBACK
                        // =====================================================

                        /*
                         * Endpoint không được định nghĩa rõ vẫn phải đăng nhập.
                         *
                         * Role và ownership chi tiết phải tiếp tục được bảo vệ
                         * bằng @PreAuthorize và kiểm tra trong service.
                         */
                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


}