package com.claimguardai.analysis;

import com.claimguardai.ai.AiProviderClient;
import com.claimguardai.ai.AiProviderException;
import com.claimguardai.ai.AiProviderRequest;
import com.claimguardai.ai.AiProviderResponse;
import com.claimguardai.ai.AiProviderType;
import com.claimguardai.claims.Claim;
import com.claimguardai.config.AppProperties;
import com.claimguardai.scoring.RiskFactorCategory;
import com.claimguardai.scoring.RiskScoringResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimAnalysisNarrativeService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimAnalysisNarrativeService.class);
    private static final String DEFAULT_DISCLAIMER =
            "This is AI-assisted review support and not a final payer decision.";

    private final AppProperties appProperties;
    private final List<AiProviderClient> aiProviderClients;
    private final FallbackAnalysisSummaryGenerator fallbackAnalysisSummaryGenerator;
    private final ObjectMapper objectMapper;

    public ClaimAnalysisNarrativeService(
            AppProperties appProperties,
            List<AiProviderClient> aiProviderClients,
            FallbackAnalysisSummaryGenerator fallbackAnalysisSummaryGenerator,
            ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.aiProviderClients = aiProviderClients;
        this.fallbackAnalysisSummaryGenerator = fallbackAnalysisSummaryGenerator;
        this.objectMapper = objectMapper;
    }

    public AiAnalysisResult buildNarrative(Claim claim, RiskScoringResult scoringResult) {
        if (!appProperties.getAi().isEnabled()) {
            return fallback(claim, scoringResult);
        }

        AiProviderType providerType = AiProviderType.from(appProperties.getAi().getProvider());
        AiProviderClient providerClient = aiProviderClients.stream()
                .collect(Collectors.toMap(AiProviderClient::supportedProvider, Function.identity()))
                .get(providerType);

        if (providerClient == null) {
            logger.warn("No AI provider client is registered for provider {}. Falling back to deterministic summary.", providerType);
            return fallback(claim, scoringResult);
        }

        try {
            AiProviderResponse providerResponse = providerClient.analyze(new AiProviderRequest(claim, scoringResult));
            return new AiAnalysisResult(
                    formatAiSummary(providerResponse),
                    toJson(providerResponse),
                    false);
        } catch (AiProviderException exception) {
            logger.warn(
                    "AI provider call failed for claim {} using provider {}. Falling back to deterministic summary. {}",
                    claim.getClaimNumber(),
                    providerType,
                    exception.getMessage());
            return fallback(claim, scoringResult);
        }
    }

    private AiAnalysisResult fallback(Claim claim, RiskScoringResult scoringResult) {
        AiProviderResponse providerResponse = new AiProviderResponse(
                fallbackAnalysisSummaryGenerator.generate(claim, scoringResult),
                scoringResult.primaryRiskReason(),
                scoringResult.factors().stream()
                        .filter(factor -> factor.category() == RiskFactorCategory.DOCUMENTATION)
                        .map(factor -> factor.description())
                        .toList(),
                scoringResult.recommendedActions(),
                scoringResult.riskCategory().name(),
                DEFAULT_DISCLAIMER);

        return new AiAnalysisResult(
                providerResponse.summary(),
                toJson(providerResponse),
                true);
    }

    private String formatAiSummary(AiProviderResponse providerResponse) {
        StringBuilder summary = new StringBuilder();
        summary.append("AI-assisted reviewer support only. ");
        summary.append(providerResponse.summary().trim());
        summary.append("\n\nRisk explanation: ").append(providerResponse.riskExplanation().trim());

        if (!providerResponse.documentationConcerns().isEmpty()) {
            summary.append("\n\nDocumentation concerns:");
            providerResponse.documentationConcerns().forEach(concern -> summary.append("\n- ").append(concern));
        }

        if (!providerResponse.recommendedActions().isEmpty()) {
            summary.append("\n\nSuggested reviewer actions:");
            providerResponse.recommendedActions().forEach(action -> summary.append("\n- ").append(action));
        }

        summary.append("\n\nReview priority: ").append(providerResponse.reviewPriority().trim());
        summary.append("\n\nDisclaimer: ").append(providerResponse.disclaimer().trim());
        return summary.toString();
    }

    private String toJson(AiProviderResponse providerResponse) {
        try {
            return objectMapper.writeValueAsString(providerResponse);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AI provider output.", exception);
        }
    }
}
