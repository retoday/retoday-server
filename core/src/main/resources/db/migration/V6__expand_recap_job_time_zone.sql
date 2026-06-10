ALTER TABLE recap_job
MODIFY COLUMN time_zone ENUM (
    'UTC',
    'SEOUL',
    'PACIFIC'
) NOT NULL;
