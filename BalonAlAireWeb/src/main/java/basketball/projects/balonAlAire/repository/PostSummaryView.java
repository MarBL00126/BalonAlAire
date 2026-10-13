package basketball.projects.balonAlAire.repository;

import java.time.LocalDateTime;

import basketball.projects.balonAlAire.model.PostStatus;

/** Campos de una noticia para listados: no incluye el contenido (HTML completo), que es lo más pesado. */
public interface PostSummaryView {
    Long getId();

    String getTitle();

    String getSlug();

    String getExcerpt();

    String getImgUrl();

    String getAuthor();

    PostStatus getStatus();

    LocalDateTime getPublishedAt();
}
