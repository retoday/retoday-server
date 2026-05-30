ALTER TABLE profile
    MODIFY language ENUM (
        'KOREAN',
        'ENGLISH',
        'JAPANESE'
        ) NOT NULL;
