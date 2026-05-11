package com.claimguardai.dashboard;

import com.claimguardai.analysis.RiskCategory;

public record RiskCategorySummaryResponse(
        RiskCategory riskCategory,
        long count) {
}
