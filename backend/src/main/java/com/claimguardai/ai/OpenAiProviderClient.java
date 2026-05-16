package com.claimguardai.ai;

import com.claimguardai.config.AppProperties;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class OpenAiProviderClient implements AiProviderClient {

    private final AppProperties appProperties;
    private final AiAnalysisPromptBuilder promptBuilder;
    private final AiAnalysisResponseParser responseParser;

    public OpenAiProviderClient(
            AppProperties appProperties,
            AiAnalysisPromptBuilder promptBuilder,
            AiAnalysisResponseParser responseParser) {
        this.appProperties = appProperties;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    @Override
    public AiProviderType supportedProvider() {
        return AiProviderType.OPENAI;
    }

    @Override
    public AiProviderResponse analyze(AiProviderRequest request) {
        try {
            StructuredResponseCreateParams<AiAnalysisStructuredOutput> createParams = ResponseCreateParams.builder()
                    .input(promptBuilder.build(request))
                    .text(AiAnalysisStructuredOutput.class)
                    .model(appProperties.getAi().getModel())
                    .build();

            AiAnalysisStructuredOutput output = buildClient()
                    .responses()
                    .create(createParams)
                    .output()
                    .stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .findFirst()
                    .orElseThrow(() -> new AiProviderException("OpenAI returned no output text."));

            return responseParser.parse(output);
        } catch (AiProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AiProviderException("OpenAI provider request failed.", exception);
        }
    }

    private OpenAIClient buildClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(appProperties.getAi().getApiKey())
                .timeout(Duration.ofSeconds(appProperties.getAi().getTimeoutSeconds()))
                .build();
    }
}
