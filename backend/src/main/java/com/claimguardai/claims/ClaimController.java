package com.claimguardai.claims;

import com.claimguardai.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateClaimRequest request) {

        ClaimResponse response = claimService.createClaim(authenticatedUser, request);
        return ResponseEntity
                .created(URI.create("/api/claims/" + response.id()))
                .body(response);
    }

    @GetMapping("/{claimId}")
    public ClaimResponse getClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return claimService.getClaim(claimId, authenticatedUser);
    }

    @GetMapping
    public List<ClaimSummaryResponse> listClaims(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return claimService.listClaims(authenticatedUser);
    }

    @PatchMapping("/{claimId}/status")
    public ClaimResponse updateClaimStatus(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ClaimStatusUpdateRequest request) {

        return claimService.updateClaimStatus(claimId, authenticatedUser, request);
    }

    @PostMapping("/{claimId}/review-notes")
    public ResponseEntity<ClaimReviewNoteResponse> addReviewNote(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ClaimReviewNoteCreateRequest request) {

        ClaimReviewNoteResponse response = claimService.addReviewNote(claimId, authenticatedUser, request);
        return ResponseEntity
                .created(URI.create("/api/claims/" + claimId + "/review-notes/" + response.id()))
                .body(response);
    }

    @GetMapping("/{claimId}/review-notes")
    public List<ClaimReviewNoteResponse> listReviewNotes(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return claimService.listReviewNotes(claimId, authenticatedUser);
    }
}
