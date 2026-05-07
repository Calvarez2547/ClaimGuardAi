package com.claimguardai.security;

import com.claimguardai.auth.AuthenticatedUser;
import com.claimguardai.config.AppProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final byte[] signingKey;

    public JwtService(ObjectMapper objectMapper, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.signingKey = resolveSigningKey(appProperties.getJwt().getSecret());
    }

    public TokenDetails generateToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(appProperties.getJwt().getExpirationMinutes(), ChronoUnit.MINUTES);

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", authenticatedUser.getUsername());
        payload.put("uid", authenticatedUser.getId());
        payload.put("roles", authenticatedUser.getRoles().stream().map(Enum::name).toList());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return new TokenDetails(unsignedToken + "." + sign(unsignedToken), expiresAt);
    }

    public String extractSubject(String token) {
        Map<String, Object> claims = parseAndValidate(token);
        Object subjectClaim = claims.get("sub");
        if (!(subjectClaim instanceof String subject) || !StringUtils.hasText(subject)) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }

        return subject;
    }

    private Map<String, Object> parseAndValidate(String token) {
        if (!StringUtils.hasText(token)) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }

        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }

        String unsignedToken = segments[0] + "." + segments[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                segments[2].getBytes(StandardCharsets.UTF_8))) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }

        Map<String, Object> header = decodeJson(segments[0]);
        if (!"HS256".equals(header.get("alg"))) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }

        Map<String, Object> claims = decodeJson(segments[1]);
        long expiration = getEpochSecondClaim(claims, "exp");
        if (Instant.now().isAfter(Instant.ofEpochSecond(expiration))) {
            throw new JwtAuthenticationException("Authentication token has expired.");
        }

        return claims;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to encode JWT payload.", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            return objectMapper.readValue(URL_DECODER.decode(value), MAP_TYPE_REFERENCE);
        } catch (IllegalArgumentException | IOException exception) {
            throw new JwtAuthenticationException("Invalid authentication token.");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("Failed to sign JWT token.", exception);
        }
    }

    private byte[] resolveSigningKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT secret must be configured.");
        }
        if (secret.trim().length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters.");
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private long getEpochSecondClaim(Map<String, Object> claims, String claimName) {
        Object claimValue = claims.get(claimName);
        if (claimValue instanceof Number numberValue) {
            return numberValue.longValue();
        }
        if (claimValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
            return Long.parseLong(stringValue);
        }
        throw new JwtAuthenticationException("Invalid authentication token.");
    }

    public record TokenDetails(String token, Instant expiresAt) {
    }
}
