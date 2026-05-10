package com.claimguardai.analysis;

import com.claimguardai.scoring.RiskFactorCategory;
import com.claimguardai.scoring.RiskFactorSeverity;

public record ClaimAnalysisFindingResponse(
        Long findingId,
        String findingCode,
        String description,
        int points,
        RiskFactorCategory category,
        String label,
        RiskFactorSeverity severity,
        int weight,
        boolean triggered,
        int contribution,
        String recommendedAction) {

    public static ClaimAnalysisFindingResponse from(ClaimAnalysisFinding finding) {
        return new ClaimAnalysisFindingResponse(
                finding.getId(),
                finding.getFindingCode(),
                finding.getDescription(),
                finding.getPoints(),
                finding.getFactorCategory(),
                finding.getFactorLabel(),
                finding.getSeverity(),
                finding.getWeight(),
                true,
                finding.getContribution(),
                finding.getRecommendedAction());
    }
}
