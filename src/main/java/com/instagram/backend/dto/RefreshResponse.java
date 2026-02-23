package com.instagram.backend.dto;

public record RefreshResponse(
        String accessToken,
        String tokenType,
        long expiresAt,
        String refreshToken,
        long refreshExpiresAt) {
}
