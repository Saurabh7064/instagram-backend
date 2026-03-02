package com.instagram.backend.dto;

public record MediaUploadResponse(
        String fileName,
        String url,
        String contentType) {
}
