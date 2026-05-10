package com.claimguardai.analysis;

import com.claimguardai.scoring.RiskFactorResult;
import com.claimguardai.scoring.RiskScoreBreakdown;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record ClaimAnalysisResponse(
        Long analysisId,
        Long claimId,
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        List<String> secondaryRiskReasons,
        List<ClaimAnalysisFindingResponse> findings,
        RiskScoreBreakdown scoreBreakdown,
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
                secondaryRiskReasons(analysis),
                analysis.getFindings().stream()
                        .map(ClaimAnalysisFindingResponse::from)
                        .toList(),
                buildScoreBreakdown(analysis),
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

    private static RiskScoreBreakdown buildScoreBreakdown(ClaimAnalysis analysis) {
        List<RiskFactorResult> factors = analysis.getFindings().stream()
                .map(finding -> new RiskFactorResult(
                        finding.getFindingCode(),
                        finding.getFactorCategory(),
                        finding.getFactorLabel(),
                        finding.getDescription(),
                        finding.getSeverity(),
                        finding.getWeight(),
                        true,
                        finding.getContribution(),
                        finding.getRecommendedAction()))
                .toList();

        int totalScore = factors.stream()
                .mapToInt(RiskFactorResult::contribution)
                .sum();

        return new RiskScoreBreakdown(
                0,
                totalScore,
                analysis.getRiskScore(),
                analysis.getRiskCategory(),
                analysis.getPrimaryRiskReason(),
                secondaryRiskReasons(analysis),
                analysis.isHumanReviewRequired(),
                factors,
                splitActions(analysis.getRecommendedActions()));
    }

    private static List<String> secondaryRiskReasons(ClaimAnalysis analysis) {
        return analysis.getFindings().stream()
                .sorted((left, right) -> Integer.compare(right.getContribution(), left.getContribution()))
                .map(ClaimAnalysisFinding::getDescription)
                .filter(description -> !description.equals(analysis.getPrimaryRiskReason()))
                .flatMap(description -> Stream.of(description))
                .toList();
    }
}
