package com.fitlife.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    @Value("${application.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public Date extractExpiration(String token) {
        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    public String generateToken(
            UserDetails userDetails
    ) {
        return generateToken(
                new HashMap<>(),
                userDetails
        );
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        if (userDetails == null
                || userDetails.getUsername() == null
                || userDetails.getUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "User details and username are required"
            );
        }

        long currentTimeMillis =
                System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(
                        new Date(currentTimeMillis)
                )
                .setExpiration(
                        new Date(
                                currentTimeMillis
                                        + jwtExpirationMs
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        if (token == null
                || token.isBlank()
                || userDetails == null
                || userDetails.getUsername() == null) {
            return false;
        }

        String tokenUsername =
                extractUsername(token);

        return tokenUsername != null
                && tokenUsername.equals(
                userDetails.getUsername()
        )
                && !isTokenExpired(token)
                && userDetails.isEnabled()
                && userDetails.isAccountNonLocked()
                && userDetails.isAccountNonExpired()
                && userDetails.isCredentialsNonExpired();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);

        return expiration == null
                || expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token must not be blank"
            );
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes =
                Decoders.BASE64.decode(jwtSecret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}