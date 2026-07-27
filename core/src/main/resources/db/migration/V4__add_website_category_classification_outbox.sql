CREATE TABLE website_category_classification_outbox
(
    id                 BINARY(16) PRIMARY KEY,
    website_id         BINARY(16)   NOT NULL,
    status             ENUM (
        'PENDING',
        'PROCESSING',
        'COMPLETED',
        'FAILED'
        )                           NOT NULL,
    attempt_count      INT          NOT NULL,
    attempted_at       TIMESTAMP(6),
    last_error_message VARCHAR(1000),
    created_at         TIMESTAMP(6) NOT NULL,
    version            BIGINT       NOT NULL
);

CREATE UNIQUE INDEX uk_website_category_classification_outbox_website_id
    ON website_category_classification_outbox (website_id);

CREATE INDEX idx_website_category_classification_outbox_polling
    ON website_category_classification_outbox (status, attempted_at, created_at);
