package com.claimguardai.analysis;

import com.claimguardai.auth.AuthenticatedUser;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimAnalysisController {

    private final ClaimAnalysisService claimAnalysisService;

    public ClaimAnalysisController(ClaimAnalysisService claimAnalysisService) {
        this.claimAnalysisService = claimAnalysisService;
    }

    @PostMapping("/{claimId}/analyze")
    public ResponseEntity<ClaimAnalysisResponse> analyzeClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ClaimAnalysisResponse response = claimAnalysisService.analyzeClaim(claimId, authenticatedUser);
        return ResponseEntity
                .created(URI.create("/api/claims/" + claimId + "/analysis/" + response.analysisId()))
                .body(response);
    }

    @GetMapping("/{claimId}/analysis/latest")
    public ClaimAnalysisResponse getLatestAnalysis(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return claimAnalysisService.getLatestAnalysis(claimId, authenticatedUser);
    }

    @GetMapping("/{claimId}/analysis/history")
    public List<ClaimAnalysisResponse> getAnalysisHistory(
            @PathVariable Long claimId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return claimAnalysisService.getAnalysisHistory(claimId, authenticatedUser);
    }
}
