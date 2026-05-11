package com.claimguardai.dashboard;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.ClaimStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LatestClaimAnalysisView(
        Long analysisId,
        Long claimId,
        String claimNumber,
        String patientControlNumber,
        ClaimStatus claimStatus,
        BigDecimal billedAmount,
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        boolean humanReviewRequired,
        Instant claimCreatedAt,
        Instant claimUpdatedAt,
        Instant analyzedAt) {
}
