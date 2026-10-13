package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.model.PostStatus;
import basketball.projects.balonAlAire.repository.PostSummaryView;

/** Fila de la tabla de noticias del panel: incluye el estado. El detalle completo se pide por id al editar. */
public record AdminPostSummaryResponse(
        Long id,
        String title,
        String slug,
        String author,
        PostStatus status,
        LocalDateTime publishedAt,
        List<CategoryResponse> categories) {

    public static AdminPostSummaryResponse from(PostSummaryView post, List<CategoryResponse> categories) {
        return new AdminPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getAuthor(),
                post.getStatus(),
                post.getPublishedAt(),
                categories);
    }
}
