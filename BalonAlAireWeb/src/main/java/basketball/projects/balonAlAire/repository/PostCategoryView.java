package basketball.projects.balonAlAire.repository;

/** Una categoría de una noticia: permite cargar las categorías de toda una página de listado en una sola consulta. */
public interface PostCategoryView {
    Long getPostId();

    Long getCategoryId();

    String getName();

    String getSlug();
}
