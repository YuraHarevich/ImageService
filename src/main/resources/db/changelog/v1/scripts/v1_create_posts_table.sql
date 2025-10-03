CREATE TABLE gallery.posts
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text VARCHAR(255) NOT NULL
);