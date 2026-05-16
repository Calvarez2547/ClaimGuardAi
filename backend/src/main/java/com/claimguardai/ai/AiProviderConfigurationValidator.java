package com.claimguardai.ai;

import com.claimguardai.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AiProviderConfigurationValidator {

    public AiProviderConfigurationValidator(AppProperties appProperties) {
        AppProperties.Ai ai = appProperties.getAi();
        if (!ai.isEnabled()) {
            return;
        }

        AiProviderType.from(ai.getProvider());

        if (!StringUtils.hasText(ai.getApiKey())) {
            throw new IllegalStateException("AI is enabled but AI_API_KEY is not configured.");
        }
        if (!StringUtils.hasText(ai.getModel())) {
            throw new IllegalStateException("AI is enabled but AI_MODEL is not configured.");
        }
    }
}
