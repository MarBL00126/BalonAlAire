-- Alinea el esquema con lo que la app ya exige (entidades y validaciones) y con los roles del panel.

UPDATE posts SET excerpt = '' WHERE excerpt IS NULL;
UPDATE posts SET img_url = '/assets/images/placeholder.jpg' WHERE img_url IS NULL;
UPDATE posts SET author = 'Redacción' WHERE author IS NULL;

ALTER TABLE posts
    ALTER COLUMN excerpt SET NOT NULL,
    ALTER COLUMN img_url SET NOT NULL,
    ALTER COLUMN author SET NOT NULL;

UPDATE social_links SET active = TRUE WHERE active IS NULL;
ALTER TABLE social_links ALTER COLUMN active SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'EDITOR'));
