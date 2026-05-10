package com.claimguardai.analysis;

import com.claimguardai.claims.Claim;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class FallbackAnalysisSummaryGenerator {

    String generate(Claim claim, RiskAssessment riskAssessment, List<RuleFinding> findings) {
        String findingSummary = findings.isEmpty()
                ? "No deterministic rule findings were identified."
                : "The main administrative concern is: " + riskAssessment.primaryRiskReason();

        return "Administrative decision support only. "
                + "This fallback summary is generated from backend-owned deterministic rules for claim "
                + claim.getClaimNumber()
                + ". "
                + findingSummary
                + " It does not make final payer, legal, billing, coding, or clinical determinations and does not override the risk score, category, findings, recommended actions, or human review decision.";
    }
}
