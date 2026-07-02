package com.claimguardai.admin;

import com.claimguardai.users.UserRole;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty Set<UserRole> roles
) {}
