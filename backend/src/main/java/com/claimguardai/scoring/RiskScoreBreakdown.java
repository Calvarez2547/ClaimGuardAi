package com.claimguardai.scoring;

import com.claimguardai.analysis.RiskCategory;
import java.util.List;

public record RiskScoreBreakdown(
        int baseScore,
        int totalScore,
        int cappedScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        List<String> secondaryRiskReasons,
        boolean humanReviewRequired,
        List<RiskFactorResult> factors,
        List<String> recommendedActions) {
}
