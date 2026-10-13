package basketball.projects.balonAlAire.dto;

/** Esquemas de URL permitidos: nunca javascript:, data:, etc. */
final class Urls {

    // Vacío permitido (campo opcional), http(s) o ruta del propio sitio (no "//host")
    static final String SITE_OR_HTTP = "^$|^(https?://|/(?!/)).+";
    static final String SITE_OR_HTTP_MESSAGE = "La URL debe empezar con http://, https:// o /";

    static final String HTTP_ONLY = "^https?://.+";
    static final String HTTP_ONLY_MESSAGE = "La URL debe empezar con http:// o https://";

    private Urls() {
    }
}
