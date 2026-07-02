package com.claimguardai;

import com.claimguardai.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
@EnableAsync
public class ClaimGuardAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimGuardAiApplication.class, args);
    }
}
