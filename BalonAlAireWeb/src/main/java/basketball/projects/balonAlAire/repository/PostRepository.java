package basketball.projects.balonAlAire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByTitle(String title);

    Optional<Post> findBySlug(String slug);

    Page<Post> findAllByOrderByPublishedAtDesc(Pageable pageable);

    List<Post> findByCategoriesSlugOrderByPublishedAtDesc(String Slug);

    @Query(
        value = """
            SELECT p.*
            FROM posts p
            WHERE p.search_vector @@ websearch_to_tsquery('spanish', :query)
            ORDER BY p.published_at DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM posts p
            WHERE p.search_vector @@ websearch_to_tsquery('spanish', :query)
            """,
        nativeQuery = true
    )
    Page<Post> fullTextSearch(
            @Param("query") String query,
            Pageable pageable
    );
}
