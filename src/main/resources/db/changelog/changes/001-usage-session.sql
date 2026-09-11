--liquibase formatted sql

--changeset iums:001-usage-session
CREATE TABLE usage_session (
    id                BIGSERIAL PRIMARY KEY,
    username          VARCHAR(32)    NOT NULL,
    mac_address       VARCHAR(17)    NOT NULL,
    start_time        TIMESTAMPTZ    NOT NULL,
    usage_seconds     INTEGER        NOT NULL,
    upload_kilobits   NUMERIC(12, 2) NOT NULL,
    download_kilobits NUMERIC(12, 2) NOT NULL,
    end_time          TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_session_username ON usage_session (username);
CREATE INDEX idx_session_user_window ON usage_session (username, start_time, end_time);
