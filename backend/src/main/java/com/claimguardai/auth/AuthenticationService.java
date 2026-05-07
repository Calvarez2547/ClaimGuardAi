package com.claimguardai.auth;

import com.claimguardai.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(),
                            request.password()));

            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            JwtService.TokenDetails tokenDetails = jwtService.generateToken(authenticatedUser);

            return new LoginResponse(
                    tokenDetails.token(),
                    "Bearer",
                    tokenDetails.expiresAt());
        } catch (AuthenticationException exception) {
            throw new AuthenticationFailedException("Invalid username or password.");
        }
    }
}
