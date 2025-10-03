CREATE TABLE gallery.comments
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    leaved_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payload   VARCHAR(255)                NOT NULL,
    post_id   UUID NOT NULL,
    poster_id UUID NOT NULL
);

ALTER TABLE gallery.comments
    ADD CONSTRAINT uk6tpd8p9v0iaw9ukuvh1gk7yj4 UNIQUE (poster_id);