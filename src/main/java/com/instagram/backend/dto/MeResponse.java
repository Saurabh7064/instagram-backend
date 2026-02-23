package com.instagram.backend.dto;

import java.time.Instant;

public record MeResponse(
        Long id,
        String fullName,
        String username,
        String email,
        Instant createdAt) {
}
