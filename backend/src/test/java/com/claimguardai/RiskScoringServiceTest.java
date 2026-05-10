package com.claimguardai;

import static org.assertj.core.api.Assertions.assertThat;

import com.claimguardai.analysis.RiskCategory;
import com.claimguardai.claims.Claim;
import com.claimguardai.scoring.RiskFactorResult;
import com.claimguardai.scoring.RiskScoringResult;
import com.claimguardai.scoring.RiskScoringService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskScoringServiceTest {

    private RiskScoringService riskScoringService;

    @BeforeEach
    void setUp() {
        riskScoringService = new RiskScoringService();
    }

    @Test
    void scoringIsDeterministicForSameClaimData() {
        Claim claim = claim("PCN-1001", true, null, "Detailed notes for deterministic scoring review.", "1250.75");

        RiskScoringResult first = riskScoringService.scoreClaim(claim);
        RiskScoringResult second = riskScoringService.scoreClaim(claim);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void missingPriorAuthorizationContributesExpectedRiskFactorAndWeight() {
        Claim claim = claim("PCN-1001", true, null, "Detailed notes for deterministic scoring review.", "1250.75");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isEqualTo(45);
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.MEDIUM);
        assertThat(result.humanReviewRequired()).isTrue();
        assertThat(result.factors())
                .singleElement()
                .satisfies(factor -> {
                    assertThat(factor.code()).isEqualTo("PRIOR_AUTH_MISSING");
                    assertThat(factor.weight()).isEqualTo(45);
                    assertThat(factor.contribution()).isEqualTo(45);
                });
    }

    @Test
    void weakDocumentationContributesExpectedRiskFactorAndWeight() {
        Claim claim = claim("PCN-1001", false, null, "Too short", "1250.75");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isEqualTo(20);
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(result.factors())
                .singleElement()
                .satisfies(factor -> {
                    assertThat(factor.code()).isEqualTo("WEAK_DOCUMENTATION_NOTES");
                    assertThat(factor.weight()).isEqualTo(20);
                    assertThat(factor.contribution()).isEqualTo(20);
                });
    }

    @Test
    void riskScoreIsCappedAtOneHundred() {
        Claim claim = claim(null, true, null, "Too short", "15000.00");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.breakdown().totalScore()).isEqualTo(105);
        assertThat(result.breakdown().cappedScore()).isEqualTo(100);
        assertThat(result.riskScore()).isEqualTo(100);
    }

    @Test
    void scoreMapsToLowRisk() {
        Claim claim = claim("PCN-1001", false, null, "Detailed notes for deterministic scoring review.", "1250.75");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isZero();
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.LOW);
    }

    @Test
    void scoreMapsToMediumRisk() {
        Claim claim = claim("PCN-1001", true, null, "Detailed notes for deterministic scoring review.", "1250.75");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isEqualTo(45);
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.MEDIUM);
    }

    @Test
    void scoreMapsToHighRisk() {
        Claim claim = claim("PCN-1001", true, null, "Too short", "15000.00");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isEqualTo(95);
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    void highRiskRequiresHumanReview() {
        Claim claim = claim("PCN-1001", true, null, "Too short", "15000.00");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskCategory()).isEqualTo(RiskCategory.HIGH);
        assertThat(result.humanReviewRequired()).isTrue();
    }

    @Test
    void recommendedActionsAreReturnedForTriggeredRiskFactors() {
        Claim claim = claim(null, true, null, "Too short", "15000.00");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.recommendedActions()).contains(
                "Verify whether prior authorization is required and attach or enter the authorization number before submission.",
                "Review claim documentation notes and add support for medical necessity or administrative context.",
                "Complete payer/member information before claim submission.",
                "Review claim amount for accuracy before continuing.",
                "Route to a human reviewer before any operational decision is finalized.");
    }

    @Test
    void mediumDocumentationOnlyRiskDoesNotRequireHumanReview() {
        Claim claim = claim(null, false, null, "Detailed enough notes for review.", "1250.75");

        RiskScoringResult result = riskScoringService.scoreClaim(claim);

        assertThat(result.riskScore()).isEqualTo(10);
        assertThat(result.riskCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(result.humanReviewRequired()).isFalse();
    }

    private Claim claim(
            String patientControlNumber,
            boolean priorAuthRequired,
            String priorAuthNumber,
            String claimNotes,
            String billedAmount) {
        Claim claim = new Claim();
        claim.setClaimNumber("CLM-TEST-1001");
        claim.setPatientControlNumber(patientControlNumber);
        claim.setPayerName("Acme Health Plan");
        claim.setProviderName("North Valley Clinic");
        claim.setServiceDate(LocalDate.of(2026, 5, 1));
        claim.setBilledAmount(new BigDecimal(billedAmount));
        claim.setPriorAuthRequired(priorAuthRequired);
        claim.setPriorAuthNumber(priorAuthNumber);
        claim.setClaimNotes(claimNotes);
        return claim;
    }
}
