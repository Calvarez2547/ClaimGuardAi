package com.claimguardai.claims;

import com.claimguardai.audit.AuditEventType;
import com.claimguardai.audit.AuditService;
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
    private final AuditService auditService;

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimReviewNoteRepository claimReviewNoteRepository,
            UserAccountRepository userAccountRepository,
            AuditService auditService) {
        this.claimRepository = claimRepository;
        this.claimReviewNoteRepository = claimReviewNoteRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
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

        ClaimResponse saved = ClaimResponse.from(claimRepository.save(claim));
        auditService.log(AuditEventType.CLAIM_CREATED, authenticatedUser.getId(),
                "Claim", String.valueOf(saved.id()), null, null,
                "Created claim #" + saved.claimNumber());
        return saved;
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
        ClaimResponse saved = ClaimResponse.from(claimRepository.save(claim));
        auditService.log(AuditEventType.CLAIM_STATUS_UPDATED, authenticatedUser.getId(),
                "Claim", String.valueOf(claimId), null, null,
                "Status updated to " + request.status() + " for claim #" + claim.getClaimNumber());
        return saved;
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

        ClaimReviewNoteResponse saved = ClaimReviewNoteResponse.from(claimReviewNoteRepository.save(note));
        auditService.log(AuditEventType.REVIEW_NOTE_ADDED, authenticatedUser.getId(),
                "Claim", String.valueOf(claimId), null, null,
                "Review note added to claim #" + claim.getClaimNumber());
        return saved;
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
