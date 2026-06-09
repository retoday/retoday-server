CREATE TABLE recap_job
(
    id            BINARY(16) PRIMARY KEY,
    user_id       BINARY(16)      NOT NULL,
    recap_date    DATE            NOT NULL,
    time_zone     ENUM ('SEOUL')  NOT NULL,
    ai_provider   ENUM ('GEMINI') NOT NULL,
    status        ENUM (
        'PENDING',
        'PROCESSING',
        'SUCCESS',
        'FAILED'
        )                         NOT NULL,
    attempts      INT             NOT NULL DEFAULT 0,
    max_attempts  INT             NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP(6)    NOT NULL,
    locked_at     TIMESTAMP(6),
    started_at    TIMESTAMP(6),
    completed_at  TIMESTAMP(6),
    failed_reason TEXT,
    created_at    TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE UNIQUE INDEX uk_recap_job_user_id_recap_date
    ON recap_job (user_id, recap_date);

CREATE INDEX idx_recap_job_status_next_retry_at
    ON recap_job (status, next_retry_at);
