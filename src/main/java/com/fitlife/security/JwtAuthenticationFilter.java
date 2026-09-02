package com.fitlife.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );

        if (!containsBearerToken(
                authorizationHeader
        )) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                extractBearerToken(
                        authorizationHeader
                );

        if (token.isBlank()) {
            SecurityContextHolder.clearContext();

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {
            authenticateIfNecessary(
                    token,
                    request
            );
        } catch (
                JwtException
                | IllegalArgumentException
                | AuthenticationException exception
        ) {
            /*
             * Không trả response trực tiếp tại filter.
             *
             * Public endpoint vẫn tiếp tục chạy.
             * Protected endpoint sẽ được Security trả 401 thông qua
             * RestAuthenticationEntryPoint.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private boolean containsBearerToken(
            String authorizationHeader
    ) {
        return authorizationHeader != null
                && authorizationHeader.startsWith(
                BEARER_PREFIX
        );
    }

    private String extractBearerToken(
            String authorizationHeader
    ) {
        return authorizationHeader
                .substring(
                        BEARER_PREFIX.length()
                )
                .trim();
    }

    private void authenticateIfNecessary(
            String token,
            HttpServletRequest request
    ) {
        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {
            return;
        }

        String principal =
                jwtService.extractUsername(
                        token
                );

        if (principal == null
                || principal.isBlank()) {
            return;
        }

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(
                                principal
                        );

        if (!jwtService.isTokenValid(
                token,
                userDetails
        )) {
            return;
        }

        /*
         * isEnabled/isAccountNonLocked được CustomUserDetails kiểm tra.
         * Token hợp lệ nhưng tài khoản đã bị khóa/inactive thì không đặt
         * Authentication vào SecurityContext.
         */
        if (!userDetails.isEnabled()
                || !userDetails.isAccountNonLocked()
                || !userDetails.isAccountNonExpired()
                || !userDetails.isCredentialsNonExpired()) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}