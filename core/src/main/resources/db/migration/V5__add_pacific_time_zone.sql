ALTER TABLE profile
    MODIFY time_zone ENUM (
        'UTC',
        'SEOUL',
        'PACIFIC'
        ) NOT NULL;
