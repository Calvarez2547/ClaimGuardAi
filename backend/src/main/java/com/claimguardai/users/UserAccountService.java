package com.claimguardai.users;

import com.claimguardai.auth.UserAlreadyExistsException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserAccount findById(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public UserAccount createUser(String username, String email, String rawPassword) {
        if (userAccountRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
        }
        if (userAccountRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username.trim());
        account.setEmail(email.trim().toLowerCase());
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setEnabled(true);
        account.setRoles(new LinkedHashSet<>(Set.of(UserRole.BILLING_SPECIALIST)));

        return userAccountRepository.save(account);
    }
}
