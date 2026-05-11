package com.claimguardai.dashboard;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.ClaimStatus;
import java.time.Instant;

public record RecentAnalysisResponse(
        Long analysisId,
        Long claimId,
        String claimNumber,
        String patientControlNumber,
        ClaimStatus status,
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        boolean humanReviewRequired,
        Instant analyzedAt) {
}
