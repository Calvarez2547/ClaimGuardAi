package com.claimguardai.claims;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ClaimResponse(
        Long id,
        String claimNumber,
        String patientControlNumber,
        String payerName,
        String providerName,
        LocalDate serviceDate,
        BigDecimal billedAmount,
        boolean priorAuthRequired,
        String priorAuthNumber,
        ClaimStatus claimStatus,
        String claimNotes,
        Long createdByUserId,
        Instant createdAt,
        Instant updatedAt) {

    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getPatientControlNumber(),
                claim.getPayerName(),
                claim.getProviderName(),
                claim.getServiceDate(),
                claim.getBilledAmount(),
                claim.isPriorAuthRequired(),
                claim.getPriorAuthNumber(),
                claim.getClaimStatus(),
                claim.getClaimNotes(),
                claim.getCreatedBy().getId(),
                claim.getCreatedAt(),
                claim.getUpdatedAt());
    }
}
