package com.claimguardai.analysis;

import com.claimguardai.claims.Claim;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class ClaimAnalysisRuleEvaluator {

    List<RuleFinding> evaluate(Claim claim) {
        List<RuleFinding> findings = new ArrayList<>();

        if (claim.isPriorAuthRequired() && isBlank(claim.getPriorAuthNumber())) {
            findings.add(new RuleFinding(
                    "PRIOR_AUTH_MISSING",
                    "Prior authorization is required but no prior authorization number is recorded.",
                    45));
        }

        if (isBlank(claim.getClaimNotes()) || claim.getClaimNotes().trim().length() < 25) {
            findings.add(new RuleFinding(
                    "WEAK_DOCUMENTATION_NOTES",
                    "Claim documentation notes are missing or too brief for confident administrative review.",
                    20));
        }

        if (isBlank(claim.getPatientControlNumber())) {
            findings.add(new RuleFinding(
                    "MISSING_PATIENT_CONTROL_NUMBER",
                    "Patient control number is missing from the claim record.",
                    10));
        }

        if (claim.getBilledAmount() != null && claim.getBilledAmount().compareTo(new BigDecimal("10000.00")) > 0) {
            findings.add(new RuleFinding(
                    "HIGH_BILLED_AMOUNT",
                    "Billed amount is unusually high for this foundation review threshold.",
                    30));
        }

        return findings;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
