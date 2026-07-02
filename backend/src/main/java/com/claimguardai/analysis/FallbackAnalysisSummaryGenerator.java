package com.claimguardai.analysis;

import com.claimguardai.claims.Claim;
import com.claimguardai.scoring.RiskScoringResult;
import org.springframework.stereotype.Component;

@Component
class FallbackAnalysisSummaryGenerator {

    String generate(Claim claim, RiskScoringResult scoringResult) {
        String findingSummary = scoringResult.factors().isEmpty()
                ? "No deterministic rule findings were identified."
                : "The main administrative concern is: " + scoringResult.primaryRiskReason();

        return "Administrative decision support only. "
                + findingSummary
                + " It does not make final payer, legal, billing, coding, or clinical determinations and does not override the risk score, category, findings, recommended actions, or human review decision.";
    }
}
