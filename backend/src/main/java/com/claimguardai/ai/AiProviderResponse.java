package com.claimguardai.ai;

import java.util.List;

public record AiProviderResponse(
        String summary,
        String riskExplanation,
        List<String> documentationConcerns,
        List<String> recommendedActions,
        String reviewPriority,
        String disclaimer) {
}
