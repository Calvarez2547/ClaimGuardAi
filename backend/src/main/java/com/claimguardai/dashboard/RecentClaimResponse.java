package com.claimguardai.dashboard;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.ClaimStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record RecentClaimResponse(
        Long claimId,
        String claimNumber,
        String patientControlNumber,
        ClaimStatus status,
        BigDecimal billedAmount,
        Instant createdAt,
        Instant updatedAt,
        Integer latestRiskScore,
        RiskCategory latestRiskCategory,
        Boolean humanReviewRequired) {
}
