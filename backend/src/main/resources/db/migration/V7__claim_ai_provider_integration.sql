ALTER TABLE claim_analyses
    ADD COLUMN IF NOT EXISTS ai_structured_output TEXT;
