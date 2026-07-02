package com.claimguardai.admin;

import com.claimguardai.users.UserAccount;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt) {

    public static AdminUserResponse from(UserAccount account) {
        return new AdminUserResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.isEnabled(),
                account.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}
