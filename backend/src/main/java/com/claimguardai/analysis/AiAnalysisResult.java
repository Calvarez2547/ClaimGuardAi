package com.claimguardai.analysis;

record AiAnalysisResult(
        String aiSummary,
        String aiStructuredOutput,
        boolean fallbackUsed) {
}
