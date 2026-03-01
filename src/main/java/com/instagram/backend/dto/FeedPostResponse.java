package com.instagram.backend.dto;

import java.time.Instant;

public record FeedPostResponse(
        Long id,
        String author,
        String authorFullName,
        String authorAvatarUrl,
        String caption,
        String imageUrl,
        String locationLabel,
        long likeCount,
        Instant createdAt) {
}
