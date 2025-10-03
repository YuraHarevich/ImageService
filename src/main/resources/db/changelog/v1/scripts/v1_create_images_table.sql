CREATE TABLE gallery.images
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url VARCHAR(500) NOT NULL,
    description TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_type VARCHAR(20) NOT NULL,
    user_id UUID,
    post_id UUID
);

ALTER TABLE gallery.images
    ADD CONSTRAINT fk_image_user
        FOREIGN KEY (user_id) REFERENCES gallery.users(id) ON DELETE CASCADE;

ALTER TABLE gallery.images
    ADD CONSTRAINT fk_image_post
        FOREIGN KEY (post_id) REFERENCES gallery.posts(id) ON DELETE CASCADE;

CREATE INDEX idx_images_user_id ON gallery.images(user_id);
CREATE INDEX idx_images_post_id ON gallery.images(post_id);