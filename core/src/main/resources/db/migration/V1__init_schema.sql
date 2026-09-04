CREATE TABLE `user` (
    id              BINARY(16) PRIMARY KEY,
    social_id       VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    social_provider ENUM('GOOGLE')  NOT NULL,
    role            ENUM(
        'ADMIN',
        'MEMBER'
    ) NOT NULL,
    status          ENUM(
        'ACTIVE'
    ) NOT NULL
);

CREATE UNIQUE INDEX uk_user_social_provider_social_id
    ON `user` (social_provider, social_id);

CREATE TABLE profile (
    id           BINARY(16) PRIMARY KEY,
    user_id      BINARY(16)   NOT NULL,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    image_url    VARCHAR(255),
    time_zone    ENUM(
        'SEOUL',
        'PACIFIC'
    ) NOT NULL,
    language     ENUM(
        'KOREAN',
        'ENGLISH',
        'JAPANESE'
    ) NOT NULL,
    recap_period TIME(6)
);

CREATE UNIQUE INDEX uk_profile_user_id
    ON profile (user_id);

CREATE TABLE website (
    id          BINARY(16) PRIMARY KEY,
    domain      VARCHAR(255) NOT NULL,
    category    ENUM(
        'STUDY',
        'SHOPPING',
        'GAMING',
        'CONTENT',
        'COMMUNITY',
        'NEWS',
        'FINANCE',
        'LIFE',
        'BROWSING',
        'DESIGN',
        'DEVELOPMENT',
        'AI',
        'ETC'
    ),
    favicon_url VARCHAR(2048)
);

CREATE UNIQUE INDEX uk_website_domain
    ON website (domain);

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
    last_attempted_at  TIMESTAMP(6),
    last_error_message VARCHAR(1000),
    created_at         TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX uk_website_category_classification_outbox_website_id
    ON website_category_classification_outbox (website_id);

CREATE INDEX idx_website_category_classification_outbox_polling
    ON website_category_classification_outbox (status, last_attempted_at, created_at);

CREATE TABLE page (
    id          BINARY(16) PRIMARY KEY,
    website_id  BINARY(16)   NOT NULL,
    url         VARCHAR(768) NOT NULL,
    title       VARCHAR(500),
    description TEXT
);

CREATE UNIQUE INDEX uk_page_url
    ON page (url);

CREATE TABLE history (
    id             BINARY(16) PRIMARY KEY,
    user_id        BINARY(16)   NOT NULL,
    website_id     BINARY(16)   NOT NULL,
    page_id        BINARY(16)   NOT NULL,
    started_at     TIMESTAMP(6) NOT NULL,
    last_active_at TIMESTAMP(6) NOT NULL,
    time_zone      ENUM(
        'UTC',
        'SEOUL',
        'PACIFIC'
    ) NOT NULL,
    ended_at       TIMESTAMP(6)
);

CREATE INDEX idx_history_user_id_started_at
    ON history (user_id, started_at);

CREATE INDEX idx_history_user_id_ended_at
    ON history (user_id, ended_at);

CREATE INDEX idx_history_ended_at_last_active_at
    ON history (ended_at, last_active_at);

CREATE TABLE user_excluded_website_domain (
    id      BINARY(16) PRIMARY KEY,
    user_id BINARY(16)   NOT NULL,
    domain  VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX uk_user_excluded_website_domain_user_id_domain
    ON user_excluded_website_domain (user_id, domain);

CREATE TABLE recap (
    id          BINARY(16) PRIMARY KEY,
    user_id     BINARY(16)      NOT NULL,
    date        DATE            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    summary     TEXT            NOT NULL,
    image       ENUM(
        'STUDY',
        'SHOPPING',
        'GAMING',
        'CONTENT',
        'COMMUNITY',
        'NEWS',
        'FINANCE',
        'LIFE',
        'BROWSING',
        'DESIGN',
        'AI',
        'DEVELOPMENT',
        'SCREEN_TIME_OVER_12H',
        'SCREEN_TIME_UNDER_1H',
        'CATEGORY_OVER_5',
        'CATEGORY_ONLY_1',
        'START_AFTER_9PM',
        'START_BEFORE_9AM',
        'RANDOM'
    ),
    started_at  TIMESTAMP(6)    NOT NULL,
    ended_at    TIMESTAMP(6)    NOT NULL,
    ai_provider ENUM(
        'GEMINI',
        'BEDROCK'
    ) NOT NULL
);

CREATE UNIQUE INDEX uk_recap_user_id_date
    ON recap (user_id, date);

CREATE TABLE recap_section (
    id       BINARY(16) PRIMARY KEY,
    recap_id BINARY(16)   NOT NULL,
    title    VARCHAR(255) NOT NULL,
    content  TEXT         NOT NULL
);

CREATE INDEX idx_recap_section_recap_id
    ON recap_section (recap_id);

CREATE TABLE recap_timeline (
    id         BINARY(16) PRIMARY KEY,
    recap_id   BINARY(16)   NOT NULL,
    title      VARCHAR(255) NOT NULL,
    started_at TIME(6)      NOT NULL,
    ended_at   TIME(6)      NOT NULL
);

CREATE INDEX idx_recap_timeline_recap_id
    ON recap_timeline (recap_id);

CREATE TABLE recap_topic (
    id       BINARY(16) PRIMARY KEY,
    recap_id BINARY(16)   NOT NULL,
    keyword  VARCHAR(255) NOT NULL,
    title    VARCHAR(255) NOT NULL,
    content  TEXT         NOT NULL
);

CREATE INDEX idx_recap_topic_recap_id
    ON recap_topic (recap_id);
