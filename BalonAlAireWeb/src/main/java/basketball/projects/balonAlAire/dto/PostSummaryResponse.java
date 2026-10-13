package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.repository.PostSummaryView;

/** Lo que necesita una tarjeta de noticia en los listados públicos (sin el contenido completo). */
public record PostSummaryResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String imgUrl,
        String author,
        LocalDateTime publishedAt,
        List<CategoryResponse> categories) {

    public static PostSummaryResponse from(PostSummaryView post, List<CategoryResponse> categories) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getImgUrl(),
                post.getAuthor(),
                post.getPublishedAt(),
                categories);
    }
}
