package com.claimguardai.claims;

import jakarta.validation.constraints.NotNull;

public record ClaimStatusUpdateRequest(
        @NotNull(message = "Claim status is required.")
        ClaimStatus status) {
}
