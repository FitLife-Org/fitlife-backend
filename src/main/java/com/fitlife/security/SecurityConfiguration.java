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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter
            jwtAuthFilter;

    private final AuthenticationProvider
            authenticationProvider;

    private final RestAuthenticationEntryPoint
            authenticationEntryPoint;

    private final RestAccessDeniedHandler
            accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
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

                                // Swagger / OpenAPI
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**"
                                ).permitAll()

                                // Common public
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Chỉ permit /test/** ở local/dev.
                                 * Không nên public khi deploy thật.
                                 */
                                .requestMatchers(
                                        "/test/**"
                                ).permitAll()

                                // Auth public
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
                                 * Logout nên có token để xác định
                                 * phiên đăng nhập hiện tại.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/logout",
                                        "/auth/logout-all"
                                ).authenticated()

                                // VNPay callback
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/payments/vnpay/return",
                                        "/payments/vnpay/ipn"
                                ).permitAll()

                                // Public packages
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/gym-packages/**",
                                        "/package-durations/**"
                                ).permitAll()

                                // CORS preflight
                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                ).permitAll()

                                // Admin / Staff operations
                                .requestMatchers(
                                        "/admin/payments/**",
                                        "/admin/equipment/**",
                                        "/admin/check-in-qrs/**"
                                ).hasAnyRole(
                                        "ADMIN",
                                        "STAFF"
                                )

                                .requestMatchers(
                                        "/check-ins/**"
                                ).hasAnyRole(
                                        "ADMIN",
                                        "STAFF"
                                )

                                // Trainer APIs
                                .requestMatchers(
                                        "/trainer/**"
                                ).hasRole("TRAINER")

                                // Member self-service check-in
                                .requestMatchers(
                                        "/member/check-ins/**",
                                        "/member/check-outs/**"
                                ).hasRole("MEMBER")

                                // Member AI
                                .requestMatchers(
                                        "/ai/suggestions/**"
                                ).hasRole("MEMBER")

                                // AI knowledge administration
                                .requestMatchers(
                                        "/admin/ai/knowledge/**"
                                ).hasRole("ADMIN")

                                // Member payments
                                .requestMatchers(
                                        "/payments/**"
                                ).hasRole("MEMBER")

                                // Member subscriptions and invoices
                                .requestMatchers(
                                        "/subscriptions/**",
                                        "/invoices/**"
                                ).hasRole("MEMBER")

                                // Member profile and body metrics
                                .requestMatchers(
                                        "/body-metrics/me/**",
                                        "/members/me/**"
                                ).hasRole("MEMBER")

                                // Member nutrition self-service
                                .requestMatchers(
                                        "/nutrition-plans/me/**"
                                ).hasRole("MEMBER")

                                // Admin nutrition management
                                .requestMatchers(
                                        "/admin/nutrition-plans/**"
                                ).hasRole("ADMIN")

                                // Workout chưa refactor hoàn toàn
                                .requestMatchers(
                                        "/workout-plans/**"
                                ).authenticated()
                                // Admin APIs còn lại
                                .requestMatchers(
                                        "/admin/**"
                                ).hasRole("ADMIN")

                                // User profile chung
                                .requestMatchers(
                                        "/users/me/**"
                                ).authenticated()

                                // Equipment public list
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/equipment/**"
                                ).authenticated()

                                // Default deny-by-authentication
                                .anyRequest()
                                .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://localhost:5175"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}