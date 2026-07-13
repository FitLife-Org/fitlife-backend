package com.fitlife.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null
                || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        if (jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticateRequestIfValid(jwt, request);
        } catch (JwtException
                 | IllegalArgumentException
                 | org.springframework.security.core.AuthenticationException exception) {

            /*
             * Không để authentication cũ tồn tại nếu token lỗi.
             *
             * Không trả response trực tiếp ở đây để Security xử lý:
             * - public endpoint vẫn có thể tiếp tục;
             * - protected endpoint sẽ trả 401 qua AuthenticationEntryPoint.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequestIfValid(
            String jwt,
            HttpServletRequest request
    ) {
        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {
            return;
        }

        String username = jwtService.extractUsername(jwt);

        if (username == null || username.isBlank()) {
            return;
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authenticationToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authenticationToken);
    }
}