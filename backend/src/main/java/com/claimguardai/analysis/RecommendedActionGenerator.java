package com.claimguardai.analysis;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class RecommendedActionGenerator {

    List<String> generate(List<RuleFinding> findings, RiskAssessment riskAssessment) {
        List<String> actions = new ArrayList<>();

        if (findings.isEmpty()) {
            actions.add("Continue standard administrative processing.");
            return actions;
        }

        for (RuleFinding finding : findings) {
            actions.add(switch (finding.findingCode()) {
                case "PRIOR_AUTH_MISSING" -> "Verify prior authorization requirements and capture the authorization number before final disposition.";
                case "WEAK_DOCUMENTATION_NOTES" -> "Request or add clearer supporting documentation notes for reviewer context.";
                case "MISSING_PATIENT_CONTROL_NUMBER" -> "Confirm and record the patient control number if available.";
                case "HIGH_BILLED_AMOUNT" -> "Route billed amount for administrative review against internal thresholds.";
                default -> "Review the flagged administrative finding.";
            });
        }

        if (riskAssessment.humanReviewRequired()) {
            actions.add("Route to a human reviewer before any operational decision is finalized.");
        }

        return actions.stream().distinct().toList();
    }
}
