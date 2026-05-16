package com.claimguardai.ai;

public interface AiProviderClient {

    AiProviderType supportedProvider();

    AiProviderResponse analyze(AiProviderRequest request);
}
