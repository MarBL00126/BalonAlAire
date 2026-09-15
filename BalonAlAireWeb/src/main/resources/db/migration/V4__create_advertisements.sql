CREATE TABLE advertisements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    image_url TEXT NOT NULL,
    link_url TEXT NOT NULL,
    position VARCHAR(100),
    active BOOLEAN NOT NULL default TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_advertisements_active
ON advertisements(active);