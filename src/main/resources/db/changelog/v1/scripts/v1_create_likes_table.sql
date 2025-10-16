CREATE TABLE gallery.likes
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    liked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    post_id  UUID NOT NULL,
    user_id  UUID NOT NULL
);