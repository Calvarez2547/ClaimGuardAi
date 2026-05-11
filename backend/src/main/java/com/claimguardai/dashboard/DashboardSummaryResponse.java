package com.claimguardai.dashboard;

import java.time.Instant;
import java.util.List;

public record DashboardSummaryResponse(
        long totalClaims,
        List<ClaimStatusSummaryResponse> claimsByStatus,
        List<RiskCategorySummaryResponse> analysesByRiskCategory,
        long humanReviewRequiredCount,
        long lowRiskCount,
        long mediumRiskCount,
        long highRiskCount,
        List<RecentClaimResponse> recentClaims,
        List<RecentAnalysisResponse> recentAnalyses,
        List<HighRiskClaimResponse> highestRiskClaims,
        List<CommonRiskFactorResponse> topRiskFactors,
        Instant generatedAt) {
}
