--liquibase formatted sql

--changeset bookshelf:002-create-users-table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(30) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(255),
    display_name    VARCHAR(255) NOT NULL,
    bio             TEXT,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ
);
