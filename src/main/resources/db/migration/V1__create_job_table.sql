CREATE TABLE job (
    id             BIGSERIAL PRIMARY KEY,
    external_id    UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    type           VARCHAR(128) NOT NULL,
    payload        JSONB,
    status         VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at     TIMESTAMP,
    finished_at    TIMESTAMP,
    worker_id      VARCHAR(256),
    claimed_at     TIMESTAMP,
    retry_count    INT NOT NULL DEFAULT 0,
    max_retries    INT NOT NULL DEFAULT 3,
    last_error     TEXT,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_job_status_scheduled ON job (status, scheduled_at) WHERE status = 'PENDING';
CREATE INDEX idx_job_worker ON job (worker_id) WHERE worker_id IS NOT NULL;