package com.claimguardai;

import com.claimguardai.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
public class ClaimGuardAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimGuardAiApplication.class, args);
    }
}
