package com.instagram.backend.dto;

import java.time.Instant;

public record ProfilePageResponse(
        Long id,
        String username,
        String fullName,
        String bio,
        long postsCount,
        long followersCount,
        long followingCount,
        Instant joinedAt) {
}
