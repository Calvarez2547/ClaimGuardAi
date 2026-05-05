package com.claimguardai.common;

import com.claimguardai.config.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final AppProperties appProperties;

    public HealthController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public HealthStatusResponse getHealth() {
        return new HealthStatusResponse(
                "UP",
                appProperties.getName(),
                appProperties.getVersion(),
                appProperties.getRuntimeEnvironment());
    }

    public record HealthStatusResponse(
            String status,
            String application,
            String version,
            String environment) {
    }
}
