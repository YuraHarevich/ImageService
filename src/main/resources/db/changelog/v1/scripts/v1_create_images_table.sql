CREATE TABLE gallery.images
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url VARCHAR(500) NOT NULL,
    name VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_type VARCHAR(20) NOT NULL,
    parent_entity_id UUID NOT NULL
);
