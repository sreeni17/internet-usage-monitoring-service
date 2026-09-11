--liquibase formatted sql

--changeset iums:002-usage-daily
CREATE TABLE usage_daily (
    username          VARCHAR(32)    NOT NULL,
    usage_date        DATE           NOT NULL,
    usage_seconds     BIGINT         NOT NULL,
    upload_kilobits   NUMERIC(18, 2) NOT NULL,
    download_kilobits NUMERIC(18, 2) NOT NULL,
    PRIMARY KEY (username, usage_date)
);

CREATE INDEX idx_daily_date ON usage_daily (usage_date);
