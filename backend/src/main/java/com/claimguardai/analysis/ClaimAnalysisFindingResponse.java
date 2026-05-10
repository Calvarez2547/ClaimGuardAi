package com.claimguardai.analysis;

public record ClaimAnalysisFindingResponse(
        Long findingId,
        String findingCode,
        String description,
        int points) {

    public static ClaimAnalysisFindingResponse from(ClaimAnalysisFinding finding) {
        return new ClaimAnalysisFindingResponse(
                finding.getId(),
                finding.getFindingCode(),
                finding.getDescription(),
                finding.getPoints());
    }
}
