package com.instagram.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.domain.UserAccount;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;

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

    public AuthenticatedPrincipal parseAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            return new AuthenticatedPrincipal(userId, username, email);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token");
        }
    }

    public record AuthToken(String accessToken, String tokenType, long expiresAt) {
    }

    public record AuthenticatedPrincipal(Long userId, String username, String email) {
    }
}
