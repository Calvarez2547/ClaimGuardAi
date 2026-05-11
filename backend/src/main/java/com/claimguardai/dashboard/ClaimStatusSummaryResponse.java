package com.claimguardai.dashboard;

import com.claimguardai.claims.ClaimStatus;

public record ClaimStatusSummaryResponse(
        ClaimStatus status,
        long count) {
}
