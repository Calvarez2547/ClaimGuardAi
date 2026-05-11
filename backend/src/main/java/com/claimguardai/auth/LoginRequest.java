package com.claimguardai.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required.")
        @Size(max = 100, message = "Username must be 100 characters or fewer.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(max = 255, message = "Password must be 255 characters or fewer.")
        String password) {
}
