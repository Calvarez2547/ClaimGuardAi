package com.claimguardai.admin;

import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import java.util.LinkedHashSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;

    public AdminUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(Pageable pageable) {
        return userAccountRepository.findAll(pageable).map(AdminUserResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        return AdminUserResponse.from(findOrThrow(userId));
    }

    @Transactional
    public AdminUserResponse updateRoles(Long userId, UpdateUserRolesRequest request) {
        UserAccount account = findOrThrow(userId);
        account.setRoles(new LinkedHashSet<>(request.roles()));
        return AdminUserResponse.from(userAccountRepository.save(account));
    }

    @Transactional
    public AdminUserResponse toggleEnabled(Long userId, ToggleUserEnabledRequest request) {
        UserAccount account = findOrThrow(userId);
        account.setEnabled(request.enabled());
        return AdminUserResponse.from(userAccountRepository.save(account));
    }

    private UserAccount findOrThrow(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
