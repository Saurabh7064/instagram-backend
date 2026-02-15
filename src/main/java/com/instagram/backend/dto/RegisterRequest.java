package com.instagram.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9._]{3,30}$") String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
