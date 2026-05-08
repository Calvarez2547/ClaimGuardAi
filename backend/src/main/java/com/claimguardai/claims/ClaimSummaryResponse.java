package com.claimguardai.claims;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ClaimSummaryResponse(
        Long id,
        String claimNumber,
        String patientControlNumber,
        String payerName,
        String providerName,
        LocalDate serviceDate,
        BigDecimal billedAmount,
        ClaimStatus claimStatus,
        Instant createdAt,
        Instant updatedAt) {

    public static ClaimSummaryResponse from(Claim claim) {
        return new ClaimSummaryResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getPatientControlNumber(),
                claim.getPayerName(),
                claim.getProviderName(),
                claim.getServiceDate(),
                claim.getBilledAmount(),
                claim.getClaimStatus(),
                claim.getCreatedAt(),
                claim.getUpdatedAt());
    }
}
