package com.instagram.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @NotBlank @Size(max = 500) String caption,
        @NotBlank @Size(max = 120) String locationLabel) {
}
