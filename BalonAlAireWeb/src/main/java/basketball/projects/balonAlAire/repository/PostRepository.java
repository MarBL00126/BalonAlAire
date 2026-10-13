package basketball.projects.balonAlAire.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.model.PostStatus;

/**
 * Los listados usan proyecciones sin la columna content. Las consultas "publicadas" fijan el estado y
 * la fecha en la propia consulta (los borradores y programados nunca salen por acá) y su orden es fijo.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Los parámetros deben seguir el orden del nombre del método (Spring Data los enlaza por posición)
    @EntityGraph(attributePaths = "categories")
    Optional<Post> findBySlugAndStatusAndPublishedAtLessThanEqual(String slug, PostStatus status, LocalDateTime now);

    String SUMMARY_COLUMNS = """
            p.id as id, p.title as title, p.slug as slug, p.excerpt as excerpt, p.imgUrl as imgUrl,
            p.author as author, p.status as status, p.publishedAt as publishedAt""";

    String PUBLISHED = "p.status = basketball.projects.balonAlAire.model.PostStatus.PUBLISHED and p.publishedAt <= :now";

    @Query(value = "select " + SUMMARY_COLUMNS + " from Post p where " + PUBLISHED
            + " order by p.publishedAt desc, p.id desc",
            countQuery = "select count(p) from Post p where " + PUBLISHED)
    Page<PostSummaryView> findPublishedSummaries(@Param("now") LocalDateTime now, Pageable pageable);

    @Query(value = "select " + SUMMARY_COLUMNS + " from Post p join p.categories c where c.slug = :slug and "
            + PUBLISHED + " order by p.publishedAt desc, p.id desc",
            countQuery = "select count(p) from Post p join p.categories c where c.slug = :slug and " + PUBLISHED)
    Page<PostSummaryView> findPublishedSummariesByCategory(
            @Param("slug") String slug, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("select " + SUMMARY_COLUMNS + " from Post p where p.id <> :postId and " + PUBLISHED
            + " and exists (select 1 from p.categories c where c.id in :categoryIds)"
            + " order by p.publishedAt desc, p.id desc")
    List<PostSummaryView> findRelatedSummaries(
            @Param("postId") Long postId,
            @Param("categoryIds") Collection<Long> categoryIds,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("select " + SUMMARY_COLUMNS + " from Post p where p.id <> :postId and " + PUBLISHED
            + " order by p.publishedAt desc, p.id desc")
    List<PostSummaryView> findLatestSummariesExcluding(
            @Param("postId") Long postId, @Param("now") LocalDateTime now, Pageable pageable);

    // Panel: incluye borradores y programados; los más nuevos primero
    @Query(value = "select " + SUMMARY_COLUMNS + " from Post p order by p.id desc",
            countQuery = "select count(p) from Post p")
    Page<PostSummaryView> findAllSummaries(Pageable pageable);

    @Query("select " + SUMMARY_COLUMNS + " from Post p where p.id in :ids")
    List<PostSummaryView> findSummariesByIdIn(@Param("ids") Collection<Long> ids);

    @Query("select p.id as postId, c.id as categoryId, c.name as name, c.slug as slug"
            + " from Post p join p.categories c where p.id in :ids order by c.id")
    List<PostCategoryView> findCategoriesOfPosts(@Param("ids") Collection<Long> ids);

    // Solo ids, ordenados por relevancia (título pesa más que resumen, y este más que el cuerpo; ver V15)
    @Query(
        value = """
            SELECT p.id
            FROM posts p
            WHERE p.status = 'PUBLISHED'
              AND p.published_at <= :now
              AND p.search_vector @@ websearch_to_tsquery('spanish', :query)
            ORDER BY ts_rank(p.search_vector, websearch_to_tsquery('spanish', :query)) DESC,
                     p.published_at DESC, p.id DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM posts p
            WHERE p.status = 'PUBLISHED'
              AND p.published_at <= :now
              AND p.search_vector @@ websearch_to_tsquery('spanish', :query)
            """,
        nativeQuery = true
    )
    Page<Long> searchPublishedIds(
            @Param("query") String query,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // Un solo DELETE: devuelve cuántas filas borró (0 = no existía). post_categories se limpia por ON DELETE CASCADE.
    @Modifying
    @Query("delete from Post p where p.id = :id")
    int deleteOneById(@Param("id") Long id);
}
