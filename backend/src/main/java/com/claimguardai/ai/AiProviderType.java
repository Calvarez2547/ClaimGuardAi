package com.claimguardai.ai;

public enum AiProviderType {
    OPENAI;

    public static AiProviderType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("AI provider must be configured when AI is enabled.");
        }

        try {
            return AiProviderType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported AI provider: " + value);
        }
    }
}
