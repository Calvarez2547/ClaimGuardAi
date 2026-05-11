package com.claimguardai.config;

import java.util.Set;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("prod")
public class ProductionConfigurationValidator implements SmartLifecycle {

    private static final Set<String> DISALLOWED_DEMO_SECRETS = Set.of(
            "local-development-jwt-secret-change-me-1234567890",
            "test-jwt-secret-change-me-1234567890");

    private final AppProperties appProperties;
    private volatile boolean running;

    public ProductionConfigurationValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void start() {
        String secret = appProperties.getJwt().getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Production profile requires JWT_SECRET to be configured.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("Production JWT secret must be at least 32 characters.");
        }
        if (DISALLOWED_DEMO_SECRETS.contains(secret)) {
            throw new IllegalStateException("Production JWT secret must not use a local or test placeholder value.");
        }
        if (appProperties.getAuth().getSeed().isEnabled()) {
            throw new IllegalStateException("Production profile must not enable the local seed user.");
        }
        if (appProperties.getSecurity().getCors().getAllowedOrigins().stream()
                .map(String::trim)
                .anyMatch("*"::equals)) {
            throw new IllegalStateException("Production CORS allowed origins must be explicit. Wildcard origins are not allowed.");
        }

        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
