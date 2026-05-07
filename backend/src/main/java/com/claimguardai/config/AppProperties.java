package com.claimguardai.config;

import com.claimguardai.users.UserRole;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name = "ClaimGuard AI Backend";
    private String version = "0.3.0";
    private String runtimeEnvironment = "default";
    private final Ai ai = new Ai();
    private final Auth auth = new Auth();
    private final Jwt jwt = new Jwt();
    private final Security security = new Security();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(String runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public Ai getAi() {
        return ai;
    }

    public Auth getAuth() {
        return auth;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Ai {

        private String apiKey = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Jwt {

        private String secret = "";
        private long expirationMinutes = 60;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }

    public static class Auth {

        private final Seed seed = new Seed();

        public Seed getSeed() {
            return seed;
        }
    }

    public static class Seed {

        private boolean enabled = false;
        private String username = "";
        private String email = "";
        private String password = "";
        private boolean accountEnabled = true;
        private List<UserRole> roles = new ArrayList<>(List.of(UserRole.USER));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAccountEnabled() {
            return accountEnabled;
        }

        public void setAccountEnabled(boolean accountEnabled) {
            this.accountEnabled = accountEnabled;
        }

        public List<UserRole> getRoles() {
            return roles;
        }

        public void setRoles(List<UserRole> roles) {
            this.roles = roles;
        }
    }

    public static class Security {

        private final Cors cors = new Cors();

        public Cors getCors() {
            return cors;
        }
    }

    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}
