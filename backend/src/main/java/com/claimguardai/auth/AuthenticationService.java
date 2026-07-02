package com.claimguardai.auth;

import com.claimguardai.audit.AuditEventType;
import com.claimguardai.audit.AuditService;
import com.claimguardai.claims.DemoClaimSeeder;
import com.claimguardai.security.JwtService;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountService userAccountService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final DemoClaimSeeder demoClaimSeeder;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserAccountService userAccountService,
            RefreshTokenService refreshTokenService,
            AuditService auditService,
            DemoClaimSeeder demoClaimSeeder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userAccountService = userAccountService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.demoClaimSeeder = demoClaimSeeder;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(),
                            request.password()));

            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            LoginResponse response = buildLoginResponse(authenticatedUser);
            auditService.log(AuditEventType.LOGIN_SUCCESS, authenticatedUser.getId(),
                    "Login successful for user: " + authenticatedUser.getUsername());
            return response;
        } catch (AuthenticationException exception) {
            auditService.log(AuditEventType.LOGIN_FAILURE,
                    "Failed login attempt for username: " + request.username());
            throw new AuthenticationFailedException("Invalid username or password.");
        }
    }

    public LoginResponse register(RegisterRequest request) {
        UserAccount account = userAccountService.createUser(
                request.username(), request.email(), request.password());
        demoClaimSeeder.seedForUser(account);
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(account);
        LoginResponse response = buildLoginResponse(authenticatedUser);
        auditService.log(AuditEventType.REGISTER, account.getId(),
                "New user registered: " + account.getUsername());
        return response;
    }

    public LoginResponse refresh(TokenRefreshRequest request) {
        RefreshToken old = refreshTokenService.verify(request.refreshToken());
        RefreshToken newToken = refreshTokenService.rotate(old);
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(newToken.getUser());
        JwtService.TokenDetails tokenDetails = jwtService.generateToken(authenticatedUser);
        auditService.log(AuditEventType.TOKEN_REFRESHED, authenticatedUser.getId(),
                "Access token refreshed for user: " + authenticatedUser.getUsername());
        return new LoginResponse(tokenDetails.token(), "Bearer", tokenDetails.expiresAt(), newToken.getToken());
    }

    public void logout(String rawRefreshToken) {
        RefreshToken rt = refreshTokenService.verify(rawRefreshToken);
        Long userId = rt.getUser().getId();
        String username = rt.getUser().getUsername();
        refreshTokenService.revokeAll(userId);
        auditService.log(AuditEventType.LOGOUT, userId, "User logged out: " + username);
    }

    private LoginResponse buildLoginResponse(AuthenticatedUser authenticatedUser) {
        JwtService.TokenDetails tokenDetails = jwtService.generateToken(authenticatedUser);
        UserAccount account = userAccountService.findById(authenticatedUser.getId());
        RefreshToken refreshToken = refreshTokenService.issue(account);
        return new LoginResponse(
                tokenDetails.token(), "Bearer", tokenDetails.expiresAt(), refreshToken.getToken());
    }
}
