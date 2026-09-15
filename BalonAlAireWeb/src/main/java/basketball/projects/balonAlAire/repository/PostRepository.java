package basketball.projects.balonAlAire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByTitle(String title);

    Optional<Post> findBySlug(String slug);

    List<Post> findAllByOrderByPublishedAtDesc();

    List<Post> findByCategoriesSlugOrderByPublishedAtDesc(String Slug);

    List<Post> findByTitleContainingIgnoreCaseOrExcerptContainingIgnoreCaseOrderByPublishedAtDesc(
            String title,
            String excrept);
}
