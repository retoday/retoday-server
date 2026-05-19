CREATE TABLE `user`
(
    id              BINARY(16) PRIMARY KEY,
    social_id       VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    social_provider ENUM ('GOOGLE') NOT NULL,
    role            ENUM (
        'ADMIN',
        'MEMBER'
        )                           NOT NULL,
    is_active       BIT(1)          NOT NULL
);

CREATE UNIQUE INDEX uk_user_social_provider_social_id
    ON `user` (social_provider, social_id);


CREATE TABLE profile
(
    id           BINARY(16) PRIMARY KEY,
    user_id      BINARY(16)     NOT NULL,
    first_name   VARCHAR(255)   NOT NULL,
    last_name    VARCHAR(255)   NOT NULL,
    image_url    VARCHAR(255)   NOT NULL,
    time_zone    ENUM ('SEOUL') NOT NULL,
    language     ENUM (
        'KOREA',
        'ENGLISH'
        )                       NOT NULL,
    recap_period TIME(6)
);

CREATE UNIQUE INDEX uk_profile_user_id
    ON profile (user_id);


CREATE TABLE website
(
    id          BINARY(16) PRIMARY KEY,
    domain      VARCHAR(255) NOT NULL,
    category    ENUM (
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
    favicon_url VARCHAR(500)
);

CREATE UNIQUE INDEX uk_website_domain
    ON website (domain);


CREATE TABLE page
(
    id          BINARY(16) PRIMARY KEY,
    website_id  BINARY(16)   NOT NULL,
    url         VARCHAR(768) NOT NULL,
    title       VARCHAR(500),
    description TEXT
);

CREATE UNIQUE INDEX uk_page_url
    ON page (url);


CREATE TABLE history
(
    id           BINARY(16) PRIMARY KEY,
    user_id      BINARY(16)   NOT NULL,
    website_id   BINARY(16)   NOT NULL,
    page_id      BINARY(16)   NOT NULL,
    visited_at   TIMESTAMP(6) NOT NULL,
    closed_at    TIMESTAMP(6) NOT NULL,
    is_closed    BIT(1)       NOT NULL,
    scroll_depth INT
);

CREATE INDEX idx_history_user_id_visited_at
    ON history (user_id, visited_at);

CREATE INDEX idx_history_user_id_closed_at
    ON history (user_id, closed_at);

CREATE INDEX idx_history_user_id_page_id_visited_at
    ON history (user_id, page_id, visited_at);


CREATE TABLE user_excluded_website_domain
(
    id         BINARY(16) PRIMARY KEY,
    user_id    BINARY(16)   NOT NULL,
    domain     VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX uk_user_excluded_website_domain_user_id_domain
    ON user_excluded_website_domain (user_id, domain);


CREATE TABLE recap
(
    id          BINARY(16) PRIMARY KEY,
    user_id     BINARY(16)      NOT NULL,
    date        DATE            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    summary     TEXT            NOT NULL,
    image       ENUM (
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
        'RANDOM_1',
        'RANDOM_2',
        'RANDOM_3'
        ),
    started_at  TIMESTAMP(6)    NOT NULL,
    ended_at    TIMESTAMP(6)    NOT NULL,
    ai_provider ENUM ('GEMINI') NOT NULL
);

CREATE UNIQUE INDEX uk_recap_user_id_date
    ON recap (user_id, date);


CREATE TABLE recap_section
(
    id         BINARY(16) PRIMARY KEY,
    recap_id   BINARY(16)   NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL
);

CREATE INDEX idx_recap_section_recap_id
    ON recap_section (recap_id);


CREATE TABLE recap_timeline
(
    id         BINARY(16) PRIMARY KEY,
    recap_id   BINARY(16)   NOT NULL,
    title      VARCHAR(255) NOT NULL,
    started_at TIME(6)      NOT NULL,
    ended_at   TIME(6)      NOT NULL
);

CREATE INDEX idx_recap_timeline_recap_id
    ON recap_timeline (recap_id);


CREATE TABLE recap_topic
(
    id         BINARY(16) PRIMARY KEY,
    recap_id   BINARY(16)   NOT NULL,
    keyword    VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL
);

CREATE INDEX idx_recap_topic_recap_id
    ON recap_topic (recap_id);
