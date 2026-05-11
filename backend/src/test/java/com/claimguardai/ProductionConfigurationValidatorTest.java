package com.claimguardai;

import static org.assertj.core.api.Assertions.assertThat;

import com.claimguardai.config.AppProperties;
import com.claimguardai.config.ProductionConfigurationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class ProductionConfigurationValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.profiles.active=prod")
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void productionProfileRequiresJwtSecret() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseMessage("Production profile requires JWT_SECRET to be configured.");
        });
    }

    @Test
    void productionProfileRejectsWildcardCorsOrigins() {
        contextRunner
                .withPropertyValues(
                        "app.jwt.secret=abcdefghijklmnopqrstuvwxyz123456",
                        "app.security.cors.allowed-origins=*")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Production CORS allowed origins must be explicit. Wildcard origins are not allowed.");
                });
    }

    @Test
    void productionProfileStartsWithExplicitSecretAndOrigins() {
        contextRunner
                .withPropertyValues(
                        "app.jwt.secret=abcdefghijklmnopqrstuvwxyz123456",
                        "app.security.cors.allowed-origins[0]=https://app.claimguardai.example")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Configuration
    @EnableConfigurationProperties(AppProperties.class)
    @Import(ProductionConfigurationValidator.class)
    static class TestConfiguration {
    }
}
