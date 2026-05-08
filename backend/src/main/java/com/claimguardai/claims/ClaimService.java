package com.claimguardai.claims;

import com.claimguardai.auth.AuthenticatedUser;
import com.claimguardai.users.UserAccount;
import com.claimguardai.users.UserAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final UserAccountRepository userAccountRepository;

    public ClaimService(
            ClaimRepository claimRepository,
            UserAccountRepository userAccountRepository) {
        this.claimRepository = claimRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public ClaimResponse createClaim(AuthenticatedUser authenticatedUser, CreateClaimRequest request) {
        UserAccount owner = userAccountRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));

        Claim claim = new Claim();
        claim.setClaimNumber(request.claimNumber().trim());
        claim.setPatientControlNumber(trimToNull(request.patientControlNumber()));
        claim.setPayerName(request.payerName().trim());
        claim.setProviderName(request.providerName().trim());
        claim.setServiceDate(request.serviceDate());
        claim.setBilledAmount(request.billedAmount());
        claim.setClaimStatus(ClaimStatus.RECEIVED);
        claim.setClaimNotes(trimToNull(request.claimNotes()));
        claim.setCreatedBy(owner);

        return ClaimResponse.from(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        return claimRepository.findByIdAndCreatedById(claimId, authenticatedUser.getId())
                .map(ClaimResponse::from)
                .orElseThrow(ClaimNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> listClaims(AuthenticatedUser authenticatedUser) {
        return claimRepository.findByCreatedByIdOrderByCreatedAtDescIdDesc(authenticatedUser.getId())
                .stream()
                .map(ClaimSummaryResponse::from)
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
