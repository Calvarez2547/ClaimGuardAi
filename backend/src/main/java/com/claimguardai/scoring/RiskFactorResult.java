package com.claimguardai.scoring;

public record RiskFactorResult(
        String code,
        RiskFactorCategory category,
        String label,
        String description,
        RiskFactorSeverity severity,
        int weight,
        boolean triggered,
        int contribution,
        String recommendedAction) {
}
