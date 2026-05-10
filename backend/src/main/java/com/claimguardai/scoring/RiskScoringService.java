package com.claimguardai.scoring;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.Claim;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    private static final int BASE_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final BigDecimal HIGH_BILLED_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final int MIN_DOCUMENTATION_LENGTH = 25;
    private static final Set<RiskFactorCode> MEDIUM_REVIEW_FACTORS =
            Set.of(RiskFactorCode.PRIOR_AUTH_MISSING, RiskFactorCode.HIGH_BILLED_AMOUNT);

    public RiskScoringResult scoreClaim(Claim claim) {
        List<RiskFactorResult> factors = new ArrayList<>();

        addIfTriggered(
                factors,
                RiskFactorCode.PRIOR_AUTH_MISSING,
                claim.isPriorAuthRequired() && isBlank(claim.getPriorAuthNumber()));

        addIfTriggered(
                factors,
                RiskFactorCode.WEAK_DOCUMENTATION_NOTES,
                isBlank(claim.getClaimNotes()) || claim.getClaimNotes().trim().length() < MIN_DOCUMENTATION_LENGTH);

        addIfTriggered(
                factors,
                RiskFactorCode.MISSING_PATIENT_CONTROL_NUMBER,
                isBlank(claim.getPatientControlNumber()));

        addIfTriggered(
                factors,
                RiskFactorCode.HIGH_BILLED_AMOUNT,
                claim.getBilledAmount() != null
                        && claim.getBilledAmount().compareTo(HIGH_BILLED_AMOUNT_THRESHOLD) > 0);

        int totalScore = BASE_SCORE + factors.stream()
                .mapToInt(RiskFactorResult::contribution)
                .sum();
        int cappedScore = Math.min(totalScore, MAX_SCORE);
        RiskCategory riskCategory = toRiskCategory(cappedScore);

        List<RiskFactorResult> orderedFactors = factors.stream()
                .sorted(Comparator.comparingInt(RiskFactorResult::contribution).reversed()
                        .thenComparing(RiskFactorResult::code))
                .toList();

        String primaryRiskReason = orderedFactors.isEmpty()
                ? "No rule findings were identified."
                : orderedFactors.getFirst().description();

        List<String> secondaryRiskReasons = orderedFactors.stream()
                .skip(1)
                .map(RiskFactorResult::description)
                .toList();

        boolean humanReviewRequired = requiresHumanReview(riskCategory, orderedFactors);
        List<String> recommendedActions = recommendedActionsFor(orderedFactors, humanReviewRequired);

        RiskScoreBreakdown breakdown = new RiskScoreBreakdown(
                BASE_SCORE,
                totalScore,
                cappedScore,
                riskCategory,
                primaryRiskReason,
                secondaryRiskReasons,
                humanReviewRequired,
                orderedFactors,
                recommendedActions);

        return new RiskScoringResult(
                cappedScore,
                riskCategory,
                primaryRiskReason,
                secondaryRiskReasons,
                humanReviewRequired,
                orderedFactors,
                recommendedActions,
                breakdown);
    }

    private boolean requiresHumanReview(RiskCategory riskCategory, List<RiskFactorResult> factors) {
        if (riskCategory == RiskCategory.HIGH) {
            return true;
        }

        if (riskCategory == RiskCategory.MEDIUM) {
            return factors.stream().anyMatch(factor -> MEDIUM_REVIEW_FACTORS.contains(RiskFactorCode.valueOf(factor.code())));
        }

        return factors.stream().anyMatch(factor -> factor.severity() == RiskFactorSeverity.CRITICAL);
    }

    private List<String> recommendedActionsFor(List<RiskFactorResult> factors, boolean humanReviewRequired) {
        LinkedHashSet<String> actions = new LinkedHashSet<>();

        if (factors.isEmpty()) {
            actions.add("Continue standard administrative processing.");
        } else {
            factors.stream()
                    .map(RiskFactorResult::recommendedAction)
                    .forEach(actions::add);
        }

        if (humanReviewRequired) {
            actions.add("Route to a human reviewer before any operational decision is finalized.");
        }

        return List.copyOf(actions);
    }

    private void addIfTriggered(List<RiskFactorResult> factors, RiskFactorCode factorCode, boolean triggered) {
        if (!triggered) {
            return;
        }

        factors.add(new RiskFactorResult(
                factorCode.name(),
                factorCode.getCategory(),
                factorCode.getLabel(),
                factorCode.getDescription(),
                factorCode.getSeverity(),
                factorCode.getWeight(),
                true,
                factorCode.getWeight(),
                factorCode.getRecommendedAction()));
    }

    private RiskCategory toRiskCategory(int riskScore) {
        if (riskScore >= 70) {
            return RiskCategory.HIGH;
        }
        if (riskScore >= 40) {
            return RiskCategory.MEDIUM;
        }
        return RiskCategory.LOW;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
