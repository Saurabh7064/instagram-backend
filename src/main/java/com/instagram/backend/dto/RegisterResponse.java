package com.instagram.backend.dto;

public record RegisterResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String accessToken,
        String tokenType,
        long expiresAt) {
}
