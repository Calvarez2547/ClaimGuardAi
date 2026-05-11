package com.claimguardai.dashboard;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.ClaimStatus;
import java.math.BigDecimal;

public record HighRiskClaimResponse(
        Long claimId,
        String claimNumber,
        String patientControlNumber,
        ClaimStatus status,
        BigDecimal billedAmount,
        int latestRiskScore,
        RiskCategory latestRiskCategory,
        String primaryRiskReason,
        boolean humanReviewRequired) {
}
