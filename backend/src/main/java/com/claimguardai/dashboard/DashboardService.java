package com.claimguardai.dashboard;

import com.claimguardai.analysis.ClaimAnalysisRepository;
import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.auth.AuthenticatedUser;
import com.claimguardai.claims.Claim;
import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.claims.ClaimStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int RECENT_CLAIMS_LIMIT = 5;
    private static final int RECENT_ANALYSES_LIMIT = 5;
    private static final int HIGHEST_RISK_CLAIMS_LIMIT = 5;
    private static final int TOP_RISK_FACTORS_LIMIT = 5;

    private final ClaimRepository claimRepository;
    private final ClaimAnalysisRepository claimAnalysisRepository;

    public DashboardService(
            ClaimRepository claimRepository,
            ClaimAnalysisRepository claimAnalysisRepository) {
        this.claimRepository = claimRepository;
        this.claimAnalysisRepository = claimAnalysisRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(AuthenticatedUser authenticatedUser) {
        Long userId = authenticatedUser.getId();
        long totalClaims = claimRepository.countByCreatedById(userId);

        List<ClaimStatusSummaryResponse> claimsByStatus = orderClaimStatuses(
                claimRepository.summarizeByStatus(userId));

        List<LatestClaimAnalysisView> latestAnalyses =
                claimAnalysisRepository.findLatestAnalysisViewsByClaimCreatedById(userId);

        List<RiskCategorySummaryResponse> analysesByRiskCategory = orderRiskCategories(latestAnalyses);
        long lowRiskCount = countByRiskCategory(latestAnalyses, RiskCategory.LOW);
        long mediumRiskCount = countByRiskCategory(latestAnalyses, RiskCategory.MEDIUM);
        long highRiskCount = countByRiskCategory(latestAnalyses, RiskCategory.HIGH);
        long humanReviewRequiredCount = latestAnalyses.stream()
                .filter(LatestClaimAnalysisView::humanReviewRequired)
                .count();

        Map<Long, LatestClaimAnalysisView> latestAnalysisByClaimId = latestAnalyses.stream()
                .collect(Collectors.toMap(
                        LatestClaimAnalysisView::claimId,
                        Function.identity()));

        List<RecentClaimResponse> recentClaims = claimRepository
                .findByCreatedByIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(0, RECENT_CLAIMS_LIMIT))
                .stream()
                .map(claim -> toRecentClaimResponse(claim, latestAnalysisByClaimId.get(claim.getId())))
                .toList();

        List<RecentAnalysisResponse> recentAnalyses = latestAnalyses.stream()
                .limit(RECENT_ANALYSES_LIMIT)
                .map(this::toRecentAnalysisResponse)
                .toList();

        List<HighRiskClaimResponse> highestRiskClaims = latestAnalyses.stream()
                .sorted(Comparator.comparingInt(LatestClaimAnalysisView::riskScore)
                        .reversed()
                        .thenComparing(LatestClaimAnalysisView::analyzedAt, Comparator.reverseOrder())
                        .thenComparing(LatestClaimAnalysisView::analysisId, Comparator.reverseOrder()))
                .limit(HIGHEST_RISK_CLAIMS_LIMIT)
                .map(this::toHighRiskClaimResponse)
                .toList();

        List<CommonRiskFactorResponse> topRiskFactors = claimAnalysisRepository
                .findTopRiskFactorsForLatestAnalysesByClaimCreatedById(userId)
                .stream()
                .limit(TOP_RISK_FACTORS_LIMIT)
                .toList();

        return new DashboardSummaryResponse(
                totalClaims,
                claimsByStatus,
                analysesByRiskCategory,
                humanReviewRequiredCount,
                lowRiskCount,
                mediumRiskCount,
                highRiskCount,
                recentClaims,
                recentAnalyses,
                highestRiskClaims,
                topRiskFactors,
                Instant.now());
    }

    private List<ClaimStatusSummaryResponse> orderClaimStatuses(List<ClaimStatusSummaryResponse> claimStatuses) {
        Map<ClaimStatus, Long> countsByStatus = new EnumMap<>(ClaimStatus.class);
        for (ClaimStatusSummaryResponse summary : claimStatuses) {
            countsByStatus.put(summary.status(), summary.count());
        }

        return Arrays.stream(ClaimStatus.values())
                .map(status -> new ClaimStatusSummaryResponse(status, countsByStatus.getOrDefault(status, 0L)))
                .filter(summary -> summary.count() > 0)
                .toList();
    }

    private List<RiskCategorySummaryResponse> orderRiskCategories(List<LatestClaimAnalysisView> latestAnalyses) {
        Map<RiskCategory, Long> countsByRiskCategory = new EnumMap<>(RiskCategory.class);
        for (LatestClaimAnalysisView latestAnalysis : latestAnalyses) {
            countsByRiskCategory.merge(latestAnalysis.riskCategory(), 1L, Long::sum);
        }

        return Arrays.stream(RiskCategory.values())
                .map(riskCategory -> new RiskCategorySummaryResponse(
                        riskCategory,
                        countsByRiskCategory.getOrDefault(riskCategory, 0L)))
                .filter(summary -> summary.count() > 0)
                .toList();
    }

    private long countByRiskCategory(List<LatestClaimAnalysisView> latestAnalyses, RiskCategory riskCategory) {
        return latestAnalyses.stream()
                .filter(analysis -> analysis.riskCategory() == riskCategory)
                .count();
    }

    private RecentClaimResponse toRecentClaimResponse(Claim claim, LatestClaimAnalysisView latestAnalysis) {
        Integer latestRiskScore = latestAnalysis != null ? latestAnalysis.riskScore() : null;
        RiskCategory latestRiskCategory = latestAnalysis != null ? latestAnalysis.riskCategory() : null;
        Boolean humanReviewRequired = latestAnalysis != null ? latestAnalysis.humanReviewRequired() : null;

        return new RecentClaimResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getPatientControlNumber(),
                claim.getClaimStatus(),
                claim.getBilledAmount(),
                claim.getCreatedAt(),
                claim.getUpdatedAt(),
                latestRiskScore,
                latestRiskCategory,
                humanReviewRequired);
    }

    private RecentAnalysisResponse toRecentAnalysisResponse(LatestClaimAnalysisView latestAnalysis) {
        return new RecentAnalysisResponse(
                latestAnalysis.analysisId(),
                latestAnalysis.claimId(),
                latestAnalysis.claimNumber(),
                latestAnalysis.patientControlNumber(),
                latestAnalysis.claimStatus(),
                latestAnalysis.riskScore(),
                latestAnalysis.riskCategory(),
                latestAnalysis.primaryRiskReason(),
                latestAnalysis.humanReviewRequired(),
                latestAnalysis.analyzedAt());
    }

    private HighRiskClaimResponse toHighRiskClaimResponse(LatestClaimAnalysisView latestAnalysis) {
        return new HighRiskClaimResponse(
                latestAnalysis.claimId(),
                latestAnalysis.claimNumber(),
                latestAnalysis.patientControlNumber(),
                latestAnalysis.claimStatus(),
                latestAnalysis.billedAmount(),
                latestAnalysis.riskScore(),
                latestAnalysis.riskCategory(),
                latestAnalysis.primaryRiskReason(),
                latestAnalysis.humanReviewRequired());
    }
}
