ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS factor_category VARCHAR(50) NOT NULL DEFAULT 'DOCUMENTATION';

ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS factor_label VARCHAR(120) NOT NULL DEFAULT 'Legacy analysis finding';

ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS weight INTEGER NOT NULL DEFAULT 0;

ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS contribution INTEGER NOT NULL DEFAULT 0;

ALTER TABLE claim_analysis_findings
    ADD COLUMN IF NOT EXISTS recommended_action VARCHAR(500) NOT NULL DEFAULT 'Review the flagged administrative finding.';

UPDATE claim_analysis_findings
SET weight = points,
    contribution = points
WHERE weight = 0
   OR contribution = 0;
