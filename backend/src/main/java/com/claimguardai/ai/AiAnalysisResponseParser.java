package com.claimguardai.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiAnalysisResponseParser {

    public AiProviderResponse parse(AiAnalysisStructuredOutput output) {
        if (output == null) {
            throw new AiProviderException("AI provider returned no structured output.");
        }

        String summary = required(output.summary, "summary");
        String riskExplanation = required(output.riskExplanation, "riskExplanation");
        List<String> documentationConcerns = normalize(output.documentationConcerns);
        List<String> recommendedActions = normalize(output.recommendedActions);
        String reviewPriority = required(output.reviewPriority, "reviewPriority");
        String disclaimer = required(output.disclaimer, "disclaimer");

        return new AiProviderResponse(
                summary,
                riskExplanation,
                documentationConcerns,
                recommendedActions,
                reviewPriority,
                disclaimer);
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new AiProviderException("AI provider returned a blank " + fieldName + " field.");
        }
        return value.trim();
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
