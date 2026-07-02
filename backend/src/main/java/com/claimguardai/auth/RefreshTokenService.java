package com.claimguardai.auth;

import com.claimguardai.users.UserAccount;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long ttlSeconds;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${app.security.refresh-token-expiry-seconds:604800}") long ttlSeconds) {
        this.repo = repo;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public RefreshToken issue(UserAccount user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        return repo.save(rt);
    }

    @Transactional(readOnly = true)
    public RefreshToken verify(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
        if (rt.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }
        return rt;
    }

    @Transactional
    public RefreshToken rotate(RefreshToken old) {
        old.setRevoked(true);
        repo.save(old);
        return issue(old.getUser());
    }

    @Transactional
    public void revokeAll(Long userId) {
        repo.revokeAllByUserId(userId);
    }
}
