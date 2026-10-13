package basketball.projects.balonAlAire.service;

import java.util.Locale;
import java.util.UUID;

/** Nombres de archivo generados por el servidor: el cliente nunca decide la ruta, solo aporta una extensión ya validada. */
final class StorageKeys {

    private StorageKeys() {
    }

    static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    static String newKey(String extension) {
        String folder = ".pdf".equals(extension) ? "downloads/" : "images/";
        return folder + UUID.randomUUID() + extension;
    }
}
