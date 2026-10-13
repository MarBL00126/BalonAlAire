package basketball.projects.balonAlAire.dto;

/** Formato de slug compartido por noticias y categorías (se usa en URLs, no admite espacios ni "/"). */
final class Slugs {

    static final String PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$";
    static final String MESSAGE = "El slug solo admite minúsculas, números y guiones (ej: liga-argentina)";

    private Slugs() {
    }
}
