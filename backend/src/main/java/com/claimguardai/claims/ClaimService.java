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
    private final ClaimReviewNoteRepository claimReviewNoteRepository;
    private final UserAccountRepository userAccountRepository;

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimReviewNoteRepository claimReviewNoteRepository,
            UserAccountRepository userAccountRepository) {
        this.claimRepository = claimRepository;
        this.claimReviewNoteRepository = claimReviewNoteRepository;
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
        claim.setPriorAuthRequired(Boolean.TRUE.equals(request.priorAuthRequired()));
        claim.setPriorAuthNumber(trimToNull(request.priorAuthNumber()));
        claim.setClaimStatus(ClaimStatus.RECEIVED);
        claim.setClaimNotes(trimToNull(request.claimNotes()));
        claim.setCreatedBy(owner);

        return ClaimResponse.from(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        return ClaimResponse.from(getOwnedClaim(claimId, authenticatedUser));
    }

    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> listClaims(AuthenticatedUser authenticatedUser) {
        return claimRepository.findByCreatedByIdOrderByCreatedAtDescIdDesc(authenticatedUser.getId())
                .stream()
                .map(ClaimSummaryResponse::from)
                .toList();
    }

    @Transactional
    public ClaimResponse updateClaimStatus(
            Long claimId,
            AuthenticatedUser authenticatedUser,
            ClaimStatusUpdateRequest request) {

        Claim claim = getOwnedClaim(claimId, authenticatedUser);
        claim.setClaimStatus(request.status());
        return ClaimResponse.from(claimRepository.save(claim));
    }

    @Transactional
    public ClaimReviewNoteResponse addReviewNote(
            Long claimId,
            AuthenticatedUser authenticatedUser,
            ClaimReviewNoteCreateRequest request) {

        Claim claim = getOwnedClaim(claimId, authenticatedUser);
        UserAccount author = userAccountRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));

        ClaimReviewNote note = new ClaimReviewNote();
        note.setClaim(claim);
        note.setAuthor(author);
        note.setNoteText(request.noteText().trim());

        return ClaimReviewNoteResponse.from(claimReviewNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<ClaimReviewNoteResponse> listReviewNotes(
            Long claimId,
            AuthenticatedUser authenticatedUser) {

        getOwnedClaim(claimId, authenticatedUser);
        return claimReviewNoteRepository.findByClaimIdAndClaimCreatedByIdOrderByCreatedAtAscIdAsc(
                        claimId,
                        authenticatedUser.getId())
                .stream()
                .map(ClaimReviewNoteResponse::from)
                .toList();
    }

    private Claim getOwnedClaim(Long claimId, AuthenticatedUser authenticatedUser) {
        return claimRepository.findByIdAndCreatedById(claimId, authenticatedUser.getId())
                .orElseThrow(ClaimNotFoundException::new);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
