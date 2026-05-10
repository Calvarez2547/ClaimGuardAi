package com.claimguardai.scoring;

public enum RiskFactorCode {
    PRIOR_AUTH_MISSING(
            RiskFactorCategory.PRIOR_AUTHORIZATION,
            "Missing prior authorization",
            RiskFactorSeverity.HIGH,
            45,
            "Prior authorization is required but no prior authorization number is recorded.",
            "Verify whether prior authorization is required and attach or enter the authorization number before submission."),

    WEAK_DOCUMENTATION_NOTES(
            RiskFactorCategory.DOCUMENTATION,
            "Weak documentation notes",
            RiskFactorSeverity.MEDIUM,
            20,
            "Claim documentation notes are missing or too brief for confident administrative review.",
            "Review claim documentation notes and add support for medical necessity or administrative context."),

    MISSING_PATIENT_CONTROL_NUMBER(
            RiskFactorCategory.MEMBER_INFORMATION,
            "Missing patient control number",
            RiskFactorSeverity.LOW,
            10,
            "Patient control number is missing from the claim record.",
            "Complete payer/member information before claim submission."),

    HIGH_BILLED_AMOUNT(
            RiskFactorCategory.CLAIM_AMOUNT,
            "High billed amount",
            RiskFactorSeverity.HIGH,
            30,
            "Billed amount is unusually high for this foundation review threshold.",
            "Review claim amount for accuracy before continuing.");

    private final RiskFactorCategory category;
    private final String label;
    private final RiskFactorSeverity severity;
    private final int weight;
    private final String description;
    private final String recommendedAction;

    RiskFactorCode(
            RiskFactorCategory category,
            String label,
            RiskFactorSeverity severity,
            int weight,
            String description,
            String recommendedAction) {
        this.category = category;
        this.label = label;
        this.severity = severity;
        this.weight = weight;
        this.description = description;
        this.recommendedAction = recommendedAction;
    }

    public RiskFactorCategory getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public RiskFactorSeverity getSeverity() {
        return severity;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
}
