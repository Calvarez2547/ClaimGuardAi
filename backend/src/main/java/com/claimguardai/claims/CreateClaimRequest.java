package com.claimguardai.claims;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotBlank(message = "Claim number is required.")
        @Size(max = 80, message = "Claim number must be 80 characters or fewer.")
        String claimNumber,

        @Size(max = 80, message = "Patient control number must be 80 characters or fewer.")
        String patientControlNumber,

        @NotBlank(message = "Payer name is required.")
        @Size(max = 255, message = "Payer name must be 255 characters or fewer.")
        String payerName,

        @NotBlank(message = "Provider name is required.")
        @Size(max = 255, message = "Provider name must be 255 characters or fewer.")
        String providerName,

        @NotNull(message = "Service date is required.")
        LocalDate serviceDate,

        @NotNull(message = "Billed amount is required.")
        @DecimalMin(value = "0.01", message = "Billed amount must be greater than zero.")
        @Digits(integer = 10, fraction = 2, message = "Billed amount must have up to 10 integer digits and 2 decimal places.")
        BigDecimal billedAmount,

        Boolean priorAuthRequired,

        @Size(max = 80, message = "Prior authorization number must be 80 characters or fewer.")
        String priorAuthNumber,

        @Size(max = 2000, message = "Claim notes must be 2000 characters or fewer.")
        String claimNotes) {
}
