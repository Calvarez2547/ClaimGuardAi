package com.claimguardai.ai;

import com.claimguardai.claims.Claim;
import com.claimguardai.scoring.RiskScoringResult;

public record AiProviderRequest(
        Claim claim,
        RiskScoringResult scoringResult) {
}
