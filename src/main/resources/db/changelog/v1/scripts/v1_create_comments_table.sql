CREATE TABLE gallery.comments
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    leaved_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payload   VARCHAR(255)                NOT NULL,
    post_id   UUID NOT NULL,
    poster_id UUID NOT NULL
);
