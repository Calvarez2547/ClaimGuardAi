package com.claimguardai.claims;

import java.time.Instant;

public record ClaimReviewNoteResponse(
        Long id,
        Long claimId,
        String noteText,
        Instant createdAt,
        Instant updatedAt) {

    public static ClaimReviewNoteResponse from(ClaimReviewNote note) {
        return new ClaimReviewNoteResponse(
                note.getId(),
                note.getClaim().getId(),
                note.getNoteText(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
