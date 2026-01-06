-- Add dispatch priority column; higher values are claimed first.
-- Defaults to 0 so existing rows are unaffected.
ALTER TABLE job ADD COLUMN IF NOT EXISTS priority INT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_job_priority_scheduled
    ON job (priority DESC, scheduled_at ASC)
    WHERE status = 'PENDING';
