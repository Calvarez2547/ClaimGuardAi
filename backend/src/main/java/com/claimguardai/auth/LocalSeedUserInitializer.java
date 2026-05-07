package com.claimguardai.auth;

import com.claimguardai.config.AppProperties;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import java.util.LinkedHashSet;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@Profile({"local", "test"})
public class LocalSeedUserInitializer implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public LocalSeedUserInitializer(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Seed seed = appProperties.getAuth().getSeed();
        if (!seed.isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(seed.getUsername())
                || !StringUtils.hasText(seed.getEmail())
                || !StringUtils.hasText(seed.getPassword())) {
            throw new IllegalStateException("Seed user is enabled but not fully configured.");
        }

        UserAccount userAccount = userAccountRepository.findByUsernameIgnoreCase(seed.getUsername())
                .orElseGet(UserAccount::new);

        userAccount.setUsername(seed.getUsername().trim());
        userAccount.setEmail(seed.getEmail().trim().toLowerCase());
        userAccount.setPasswordHash(passwordEncoder.encode(seed.getPassword()));
        userAccount.setEnabled(seed.isAccountEnabled());
        userAccount.setRoles(new LinkedHashSet<>(seed.getRoles()));

        userAccountRepository.save(userAccount);
    }
}
