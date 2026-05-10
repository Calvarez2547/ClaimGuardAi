package com.claimguardai.scoring;

import com.claimguardai.analysis.RiskCategory;
import java.util.List;

public record RiskScoringResult(
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        List<String> secondaryRiskReasons,
        boolean humanReviewRequired,
        List<RiskFactorResult> factors,
        List<String> recommendedActions,
        RiskScoreBreakdown breakdown) {
}
