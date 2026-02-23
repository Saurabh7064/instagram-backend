package com.instagram.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.instagram.backend.domain.UserAccount;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

    private final SecretKey signingKey;
    private final long accessTokenTtlMinutes;

    public TokenService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public AuthToken createAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new AuthToken(token, "Bearer", expiresAt.toEpochMilli());
    }

    public record AuthToken(String accessToken, String tokenType, long expiresAt) {
    }
}
