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

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

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

                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // Swagger / OpenAPI
                        // =====================================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // =====================================
                        // Common public endpoints
                        // =====================================
                        .requestMatchers(
                                "/error",
                                "/test/**"
                        ).permitAll()

                        // =====================================
                        // Public Auth APIs
                        // =====================================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/google-login",
                                "/auth/refresh-token",
                                "/auth/logout",
                                "/auth/resend-verification-email",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/verify-email"
                        ).permitAll()

                        /*
                         * Logout all phải có access token.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/logout-all"
                        ).authenticated()

                        // =====================================
                        // VNPay callback
                        // =====================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/payments/vnpay/return",
                                "/payments/vnpay/ipn"
                        ).permitAll()

                        // =====================================
                        // Public Gym Package APIs
                        // =====================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/gym-packages/**",
                                "/package-durations/**"
                        ).permitAll()

                        // =====================================
                        // CORS preflight
                        // =====================================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =====================================
                        // Admin / Staff payment
                        // =====================================
                        .requestMatchers(
                                "/admin/payments/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "STAFF"
                        )

                        // =====================================
                        // Admin / Staff equipment
                        // =====================================
                        .requestMatchers(
                                "/admin/equipment/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "STAFF"
                        )

                        // =====================================
                        // Member payment
                        // =====================================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/payments/vnpay/create-url"
                        ).hasRole("MEMBER")

                        .requestMatchers(
                                "/payments/**"
                        ).hasRole("MEMBER")

                        // =====================================
                        // Admin APIs
                        // =====================================
                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // Other APIs
                        // =====================================
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