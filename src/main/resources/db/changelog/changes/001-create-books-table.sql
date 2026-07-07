--liquibase formatted sql

--changeset bookshelf:001-create-books-table
CREATE TABLE books (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    author      VARCHAR(255) NOT NULL,
    genre       VARCHAR(255) NOT NULL,
    status      VARCHAR(32)  NOT NULL,
    rating      INTEGER,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ
);
