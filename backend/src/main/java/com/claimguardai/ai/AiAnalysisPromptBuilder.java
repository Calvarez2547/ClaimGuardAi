package com.claimguardai.ai;

import com.claimguardai.claims.Claim;
import com.claimguardai.scoring.RiskFactorResult;
import com.claimguardai.scoring.RiskScoringResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiAnalysisPromptBuilder {

    public String build(AiProviderRequest request) {
        Claim claim = request.claim();
        RiskScoringResult scoringResult = request.scoringResult();

        StringBuilder prompt = new StringBuilder();
        prompt.append("This is a healthcare revenue-cycle claim review prototype.\n");
        prompt.append("The AI is assisting a reviewer.\n");
        prompt.append("The AI is not making medical decisions.\n");
        prompt.append("The AI is not giving clinical advice.\n");
        prompt.append("The AI is not making legal decisions.\n");
        prompt.append("The AI is not making final payer decisions.\n");
        prompt.append("The AI is not replacing a human reviewer.\n");
        prompt.append("The AI must not invent facts.\n");
        prompt.append("The AI must only use the provided claim fields and backend deterministic scoring context.\n");
        prompt.append("Return structured JSON only.\n");
        prompt.append("Do not include markdown, code fences, or commentary outside the JSON object.\n\n");

        prompt.append("Required JSON shape:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"...\",\n");
        prompt.append("  \"riskExplanation\": \"...\",\n");
        prompt.append("  \"documentationConcerns\": [\"...\"],\n");
        prompt.append("  \"recommendedActions\": [\"...\"],\n");
        prompt.append("  \"reviewPriority\": \"HIGH\",\n");
        prompt.append("  \"disclaimer\": \"This is AI-assisted review support and not a final payer decision.\"\n");
        prompt.append("}\n\n");

        prompt.append("Claim fields:\n");
        appendField(prompt, "claimNumber", claim.getClaimNumber());
        appendField(prompt, "patientControlNumber", claim.getPatientControlNumber());
        appendField(prompt, "payerName", claim.getPayerName());
        appendField(prompt, "providerName", claim.getProviderName());
        appendField(prompt, "serviceDate", claim.getServiceDate() != null ? claim.getServiceDate().toString() : null);
        appendField(prompt, "billedAmount", claim.getBilledAmount() != null ? claim.getBilledAmount().toPlainString() : null);
        appendField(prompt, "priorAuthRequired", String.valueOf(claim.isPriorAuthRequired()));
        appendField(prompt, "priorAuthNumber", claim.getPriorAuthNumber());
        appendField(prompt, "claimStatus", claim.getClaimStatus() != null ? claim.getClaimStatus().name() : null);
        appendField(prompt, "claimNotes", claim.getClaimNotes());

        prompt.append("\nBackend deterministic scoring context:\n");
        appendField(prompt, "riskScore", String.valueOf(scoringResult.riskScore()));
        appendField(prompt, "riskCategory", scoringResult.riskCategory().name());
        appendField(prompt, "primaryRiskReason", scoringResult.primaryRiskReason());
        appendList(prompt, "secondaryRiskReasons", scoringResult.secondaryRiskReasons());
        appendField(prompt, "humanReviewRequired", String.valueOf(scoringResult.humanReviewRequired()));
        appendList(prompt, "backendRecommendedActions", scoringResult.recommendedActions());

        prompt.append("Triggered deterministic factors:\n");
        if (scoringResult.factors().isEmpty()) {
            prompt.append("- none\n");
        } else {
            for (RiskFactorResult factor : scoringResult.factors()) {
                prompt.append("- code=").append(factor.code())
                        .append(", category=").append(factor.category())
                        .append(", label=").append(factor.label())
                        .append(", severity=").append(factor.severity())
                        .append(", weight=").append(factor.weight())
                        .append(", contribution=").append(factor.contribution())
                        .append(", description=").append(factor.description())
                        .append(", recommendedAction=").append(factor.recommendedAction())
                        .append('\n');
            }
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Keep the summary concise and reviewer-oriented.\n");
        prompt.append("- The riskExplanation must explain the deterministic score in plain language.\n");
        prompt.append("- documentationConcerns should only mention documentation gaps supported by the input.\n");
        prompt.append("- recommendedActions should help a reviewer, but must not contradict deterministic backend actions.\n");
        prompt.append("- reviewPriority should be LOW, MEDIUM, or HIGH.\n");
        prompt.append("- The disclaimer must explicitly state that this is AI-assisted review support and not a final payer decision.\n");

        return prompt.toString();
    }

    private void appendField(StringBuilder prompt, String fieldName, String value) {
        prompt.append("- ").append(fieldName).append(": ")
                .append(value == null || value.isBlank() ? "(not provided)" : value.trim())
                .append('\n');
    }

    private void appendList(StringBuilder prompt, String fieldName, List<String> values) {
        prompt.append("- ").append(fieldName).append(": ");
        if (values == null || values.isEmpty()) {
            prompt.append("(none)\n");
            return;
        }

        prompt.append(String.join(" | ", values)).append('\n');
    }
}
