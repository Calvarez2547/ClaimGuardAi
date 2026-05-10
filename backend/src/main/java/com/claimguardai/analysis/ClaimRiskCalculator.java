package com.claimguardai.analysis;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class ClaimRiskCalculator {

    RiskAssessment calculate(List<RuleFinding> findings) {
        int riskScore = findings.stream()
                .mapToInt(RuleFinding::points)
                .sum();
        riskScore = Math.min(riskScore, 100);

        RiskCategory riskCategory = toCategory(riskScore);
        String primaryRiskReason = findings.stream()
                .max(Comparator.comparingInt(RuleFinding::points))
                .map(RuleFinding::description)
                .orElse("No rule findings were identified.");

        return new RiskAssessment(
                riskScore,
                riskCategory,
                primaryRiskReason,
                riskCategory != RiskCategory.LOW);
    }

    private RiskCategory toCategory(int riskScore) {
        if (riskScore >= 70) {
            return RiskCategory.HIGH;
        }
        if (riskScore >= 40) {
            return RiskCategory.MEDIUM;
        }
        return RiskCategory.LOW;
    }
}
