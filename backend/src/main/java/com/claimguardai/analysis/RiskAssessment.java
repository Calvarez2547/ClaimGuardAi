package com.claimguardai.analysis;

record RiskAssessment(
        int riskScore,
        RiskCategory riskCategory,
        String primaryRiskReason,
        boolean humanReviewRequired) {
}
