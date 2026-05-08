package com.claimguardai.claims;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimReviewNoteCreateRequest(
        @NotBlank(message = "Review note text is required.")
        @Size(max = 2000, message = "Review note text must be 2000 characters or fewer.")
        String noteText) {
}
