package com.claimguardai.analysis;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record ClaimAnalysisResponse(
        Long analysisId,
        Long claimId,
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        List<ClaimAnalysisFindingResponse> findings,
        String aiSummary,
        List<String> recommendedActions,
        boolean humanReviewRequired,
        boolean fallbackUsed,
        Instant createdAt) {

    public static ClaimAnalysisResponse from(ClaimAnalysis analysis) {
        return new ClaimAnalysisResponse(
                analysis.getId(),
                analysis.getClaim().getId(),
                analysis.getRiskScore(),
                analysis.getRiskCategory(),
                analysis.getPrimaryRiskReason(),
                analysis.getFindings().stream()
                        .map(ClaimAnalysisFindingResponse::from)
                        .toList(),
                analysis.getAiSummary(),
                splitActions(analysis.getRecommendedActions()),
                analysis.isHumanReviewRequired(),
                analysis.isFallbackUsed(),
                analysis.getCreatedAt());
    }

    private static List<String> splitActions(String recommendedActions) {
        if (recommendedActions == null || recommendedActions.isBlank()) {
            return List.of();
        }

        return Arrays.stream(recommendedActions.split("\\R"))
                .map(String::trim)
                .filter(action -> !action.isEmpty())
                .toList();
    }
}
