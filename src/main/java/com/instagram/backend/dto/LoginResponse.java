package com.instagram.backend.dto;

public record LoginResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String accessToken,
        String tokenType,
        long expiresAt) {
}
