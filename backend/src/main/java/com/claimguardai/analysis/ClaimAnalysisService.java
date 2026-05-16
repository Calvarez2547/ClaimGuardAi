package com.claimguardai.analysis;

import com.claimguardai.auth.AuthenticatedUser;
import com.claimguardai.claims.Claim;
import com.claimguardai.claims.ClaimNotFoundException;
import com.claimguardai.claims.ClaimRepository;
import com.claimguardai.scoring.RiskFactorResult;
import com.claimguardai.scoring.RiskScoringResult;
import com.claimguardai.scoring.RiskScoringService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimAnalysisService {

    private final ClaimRepository claimRepository;
    private final ClaimAnalysisRepository claimAnalysisRepository;
    private final RiskScoringService riskScoringService;
    private final ClaimAnalysisNarrativeService claimAnalysisNarrativeService;

    public ClaimAnalysisService(
            ClaimRepository claimRepository,
            ClaimAnalysisRepository claimAnalysisRepository,
            RiskScoringService riskScoringService,
            ClaimAnalysisNarrativeService claimAnalysisNarrativeService) {
        this.claimRepository = claimRepository;
        this.claimAnalysisRepository = claimAnalysisRepository;
        this.riskScoringService = riskScoringService;
        this.claimAnalysisNarrativeService = claimAnalysisNarrativeService;
    }

    @Transactional
    public ClaimAnalysisResponse analyzeClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        Claim claim = getOwnedClaim(claimId, authenticatedUser);
        RiskScoringResult scoringResult = riskScoringService.scoreClaim(claim);
        AiAnalysisResult aiAnalysisResult = claimAnalysisNarrativeService.buildNarrative(claim, scoringResult);

        ClaimAnalysis analysis = new ClaimAnalysis();
        analysis.setClaim(claim);
        analysis.setRiskScore(scoringResult.riskScore());
        analysis.setRiskCategory(scoringResult.riskCategory());
        analysis.setPrimaryRiskReason(scoringResult.primaryRiskReason());
        analysis.setRecommendedActions(String.join("\n", scoringResult.recommendedActions()));
        analysis.setHumanReviewRequired(scoringResult.humanReviewRequired());
        analysis.setFallbackUsed(aiAnalysisResult.fallbackUsed());
        analysis.setAiSummary(aiAnalysisResult.aiSummary());
        analysis.setAiStructuredOutput(aiAnalysisResult.aiStructuredOutput());

        for (RiskFactorResult factor : scoringResult.factors()) {
            ClaimAnalysisFinding finding = new ClaimAnalysisFinding();
            finding.setFindingCode(factor.code());
            finding.setDescription(factor.description());
            finding.setPoints(factor.contribution());
            finding.setFactorCategory(factor.category());
            finding.setFactorLabel(factor.label());
            finding.setSeverity(factor.severity());
            finding.setWeight(factor.weight());
            finding.setContribution(factor.contribution());
            finding.setRecommendedAction(factor.recommendedAction());
            analysis.addFinding(finding);
        }

        return ClaimAnalysisResponse.from(claimAnalysisRepository.save(analysis));
    }

    @Transactional(readOnly = true)
    public ClaimAnalysisResponse getLatestAnalysis(Long claimId, AuthenticatedUser authenticatedUser) {
        getOwnedClaim(claimId, authenticatedUser);
        return claimAnalysisRepository.findFirstByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
                        claimId,
                        authenticatedUser.getId())
                .map(ClaimAnalysisResponse::from)
                .orElseThrow(ClaimNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ClaimAnalysisResponse> getAnalysisHistory(Long claimId, AuthenticatedUser authenticatedUser) {
        getOwnedClaim(claimId, authenticatedUser);
        return claimAnalysisRepository.findByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
                        claimId,
                        authenticatedUser.getId())
                .stream()
                .map(ClaimAnalysisResponse::from)
                .toList();
    }

    private Claim getOwnedClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        return claimRepository.findByIdAndCreatedById(claimId, authenticatedUser.getId())
                .orElseThrow(ClaimNotFoundException::new);
    }
}
