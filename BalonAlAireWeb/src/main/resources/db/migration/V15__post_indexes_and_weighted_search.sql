-- Rendimiento y búsqueda de noticias.

-- Búsqueda ponderada: título (A) > resumen (B) > cuerpo (C). La columna generada no se puede alterar: se recrea.
DROP INDEX IF EXISTS idx_posts_search;
ALTER TABLE posts DROP COLUMN search_vector;

ALTER TABLE posts
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('spanish', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('spanish', coalesce(excerpt, '')), 'B') ||
    -- El límite evita superar el tamaño máximo de un tsvector con artículos enormes
    setweight(to_tsvector('spanish', coalesce(left(content, 200000), '')), 'C')
) STORED;

CREATE INDEX idx_posts_search
ON posts USING GIN(search_vector);

-- Listado principal: WHERE status = 'PUBLISHED' AND published_at <= now ORDER BY published_at DESC
CREATE INDEX idx_posts_published
ON posts (published_at DESC, id DESC)
WHERE status = 'PUBLISHED';

-- Noticias por categoría; reemplaza al índice solo por category_id
CREATE INDEX idx_post_categories_category_post
ON post_categories (category_id, post_id);

DROP INDEX IF EXISTS idx_post_categories_category_id;
DROP INDEX IF EXISTS idx_posts_published_at;
