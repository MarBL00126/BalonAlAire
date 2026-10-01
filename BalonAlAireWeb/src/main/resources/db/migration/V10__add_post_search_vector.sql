ALTER TABLE posts
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    to_tsvector(
        'spanish',
        coalesce(title, '') || ' ' || coalesce(excerpt, '')
    )
) STORED;

CREATE INDEX idx_posts_search
ON posts USING GIN(search_vector);