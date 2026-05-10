package com.claimguardai.analysis;

import com.claimguardai.auth.AuthenticatedUser;
import com.claimguardai.claims.Claim;
import com.claimguardai.claims.ClaimNotFoundException;
import com.claimguardai.claims.ClaimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimAnalysisService {

    private final ClaimRepository claimRepository;
    private final ClaimAnalysisRepository claimAnalysisRepository;
    private final ClaimAnalysisRuleEvaluator ruleEvaluator;
    private final ClaimRiskCalculator riskCalculator;
    private final FallbackAnalysisSummaryGenerator summaryGenerator;
    private final RecommendedActionGenerator recommendedActionGenerator;

    public ClaimAnalysisService(
            ClaimRepository claimRepository,
            ClaimAnalysisRepository claimAnalysisRepository,
            ClaimAnalysisRuleEvaluator ruleEvaluator,
            ClaimRiskCalculator riskCalculator,
            FallbackAnalysisSummaryGenerator summaryGenerator,
            RecommendedActionGenerator recommendedActionGenerator) {
        this.claimRepository = claimRepository;
        this.claimAnalysisRepository = claimAnalysisRepository;
        this.ruleEvaluator = ruleEvaluator;
        this.riskCalculator = riskCalculator;
        this.summaryGenerator = summaryGenerator;
        this.recommendedActionGenerator = recommendedActionGenerator;
    }

    @Transactional
    public ClaimAnalysisResponse analyzeClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        Claim claim = getOwnedClaim(claimId, authenticatedUser);
        List<RuleFinding> ruleFindings = ruleEvaluator.evaluate(claim);
        RiskAssessment riskAssessment = riskCalculator.calculate(ruleFindings);
        List<String> recommendedActions = recommendedActionGenerator.generate(ruleFindings, riskAssessment);

        ClaimAnalysis analysis = new ClaimAnalysis();
        analysis.setClaim(claim);
        analysis.setRiskScore(riskAssessment.riskScore());
        analysis.setRiskCategory(riskAssessment.riskCategory());
        analysis.setPrimaryRiskReason(riskAssessment.primaryRiskReason());
        analysis.setRecommendedActions(String.join("\n", recommendedActions));
        analysis.setHumanReviewRequired(riskAssessment.humanReviewRequired());
        analysis.setFallbackUsed(true);
        analysis.setAiSummary(summaryGenerator.generate(claim, riskAssessment, ruleFindings));

        for (RuleFinding ruleFinding : ruleFindings) {
            ClaimAnalysisFinding finding = new ClaimAnalysisFinding();
            finding.setFindingCode(ruleFinding.findingCode());
            finding.setDescription(ruleFinding.description());
            finding.setPoints(ruleFinding.points());
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
