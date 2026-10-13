package basketball.projects.balonAlAire.dto;

/** Política mínima de contraseñas del panel (la misma para alta, cambio y recuperación). */
final class Passwords {

    static final int MIN_LENGTH = 8;
    static final String LENGTH_MESSAGE = "La contraseña debe tener entre 8 y 100 caracteres";

    private Passwords() {
    }
}
