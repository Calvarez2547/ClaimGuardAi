package com.claimguardai;

import static org.assertj.core.api.Assertions.assertThat;

import com.claimguardai.ai.AiProviderConfigurationValidator;
import com.claimguardai.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class AiProviderConfigurationValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void enabledAiRequiresApiKey() {
        contextRunner
                .withPropertyValues(
                        "app.ai.enabled=true",
                        "app.ai.provider=OPENAI",
                        "app.ai.model=gpt-4o-mini")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("AI is enabled but AI_API_KEY is not configured.");
                });
    }

    @Test
    void enabledAiRequiresModel() {
        contextRunner
                .withPropertyValues(
                        "app.ai.enabled=true",
                        "app.ai.provider=OPENAI",
                        "app.ai.api-key=test-api-key",
                        "app.ai.model=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("AI is enabled but AI_MODEL is not configured.");
                });
    }

    @Test
    void disabledAiAllowsMissingApiKey() {
        contextRunner.run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void enabledAiStartsWhenRequiredPropertiesArePresent() {
        contextRunner
                .withPropertyValues(
                        "app.ai.enabled=true",
                        "app.ai.provider=OPENAI",
                        "app.ai.api-key=test-api-key",
                        "app.ai.model=gpt-4o-mini",
                        "app.ai.timeout-seconds=5")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Configuration
    @EnableConfigurationProperties(AppProperties.class)
    @Import(AiProviderConfigurationValidator.class)
    static class TestConfiguration {
    }
}
