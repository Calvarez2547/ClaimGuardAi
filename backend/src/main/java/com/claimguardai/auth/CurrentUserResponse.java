package com.claimguardai.auth;

import java.util.Set;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        Set<String> roles) {

    public static CurrentUserResponse from(AuthenticatedUser authenticatedUser) {
        Set<String> roles = authenticatedUser.getRoles()
                .stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        return new CurrentUserResponse(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail(),
                roles);
    }
}
