-- categories (slugs match the links used in the header nav menu)
INSERT INTO categories (name,slug) VALUES
('Basquet Nacional','basquet-nacional'),
('Liga Argentina','liga-argentina'),
('Magazine','magazine'),
('Blog','blog'),
('Fiebre NCAA','fiebre-ncaa'),
('Internacional','internacional');

-- default admin user, password hash for "ChangeMe123!" (BCrypt, cost 10) — change it after first login
INSERT INTO users (username, password, role) VALUES
('admin', '$2b$10$2x1BkDH5QIddyjt4b8xs4eLhWPPIG56if/8IxCUo8VfNITSu1jYS6', 'ADMIN');

INSERT INTO posts (title, slug, excerpt, content, img_url, author, status, published_at) VALUES
('La Liga Argentina define sus semifinales', 'liga-argentina-define-semifinales',
 'Los cuatro equipos que buscan el título ya están confirmados.',
 'Con la fase regular terminada, la Liga Argentina de Básquet ya tiene a sus cuatro semifinalistas. En esta nota repasamos los cruces, las fechas confirmadas y los candidatos al título.',
 '/assets/images/posts/liga-semifinales.jpg', 'Juan Pérez', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('Básquet Nacional: el ascenso que sorprendió a todos', 'basquet-nacional-ascenso-sorpresa',
 'Un equipo del interior logró subir de categoría tras una campaña histórica.',
 'El Torneo Nacional de Ascenso tuvo su final más pareja de los últimos años. Repasamos cómo se dio la clasificación y qué se viene para el club en la próxima temporada.',
 '/assets/images/posts/nacional-ascenso.jpg', 'María Gómez', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '2 day'),
('Magazine: los mejores highlights de la fecha', 'magazine-highlights-de-la-fecha',
 'Las mejores jugadas y estadísticas de la última jornada.',
 'Un repaso por las mejores jugadas, triples y bloqueos de la fecha, con las estadísticas destacadas de cada partido.',
 '/assets/images/posts/magazine-highlights.jpg', 'Redacción', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '3 day'),
('Internacional: la NBA arrancó su temporada', 'internacional-nba-arranco-temporada',
 'Los equipos favoritos ya debutaron en la nueva temporada regular.',
 'La NBA comenzó su temporada regular con triunfos ajustados y algunas sorpresas. Contamos los resultados de la primera fecha y qué jugadores argentinos ya debutaron.',
 '/assets/images/posts/nba-temporada.jpg', 'Redacción', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '4 day');

-- link each seed post to its category by slug, so it doesn't depend on auto-generated ids
INSERT INTO post_categories (post_id, category_id)
SELECT p.id, c.id FROM posts p, categories c
WHERE (p.slug, c.slug) IN (
    ('liga-argentina-define-semifinales', 'liga-argentina'),
    ('basquet-nacional-ascenso-sorpresa', 'basquet-nacional'),
    ('magazine-highlights-de-la-fecha', 'magazine'),
    ('internacional-nba-arranco-temporada', 'internacional')
);

INSERT INTO advertisements (name, image_url, link_url, position, active) VALUES
('Banner superior sponsor', '/assets/images/ads/banner-header.jpg', 'https://sponsor-ejemplo.com', 'header-banner', true),
('Publicidad lateral', '/assets/images/ads/sidebar-ad.jpg', 'https://sponsor-ejemplo.com', 'sidebar', true);

INSERT INTO social_links (platform, link_url, active) VALUES
('youtube', 'https://youtube.com/@balonalaireweb', true),
('instagram', 'https://instagram.com/balonalaire', true),
('twitter', 'https://x.com/balonalaire', true);

INSERT INTO site_settings (settings_key, settings_value)
VALUES
('site_name', 'Balón al Aire'),
('site_description', 'Noticias de básquet'),
('contact_email', 'contacto@balonalaire.com'),
('logo_url', '/assets/images/logo.png'),
('footer_text', '© 2026 Balón al Aire');
