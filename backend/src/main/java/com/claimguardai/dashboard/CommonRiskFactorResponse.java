package com.claimguardai.dashboard;

import com.claimguardai.scoring.RiskFactorCategory;

public record CommonRiskFactorResponse(
        String code,
        RiskFactorCategory category,
        String label,
        long count,
        long totalContribution) {
}
