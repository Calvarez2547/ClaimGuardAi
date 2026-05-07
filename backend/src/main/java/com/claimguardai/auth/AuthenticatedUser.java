package com.claimguardai.auth;

import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserRole;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<UserRole> roles;
    private final Collection<? extends GrantedAuthority> authorities;

    private AuthenticatedUser(
            Long id,
            String username,
            String email,
            String passwordHash,
            boolean enabled,
            Set<UserRole> roles,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = Set.copyOf(roles);
        this.authorities = authorities;
    }

    public static AuthenticatedUser from(UserAccount userAccount) {
        Set<UserRole> roles = new LinkedHashSet<>(userAccount.getRoles());
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .map(GrantedAuthority.class::cast)
                .toList();

        return new AuthenticatedUser(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                userAccount.isEnabled(),
                roles,
                authorities);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
