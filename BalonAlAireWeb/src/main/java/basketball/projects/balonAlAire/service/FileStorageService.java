package basketball.projects.balonAlAire.service;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** Guarda el archivo (ya validado) y devuelve la URL con la que se sirve. */
    String upload(MultipartFile file);

    /**
     * Prepara una subida directa del navegador al storage para no ocupar un hilo del servidor.
     * Vacío si la implementación no la soporta (storage local): el cliente debe usar {@link #upload}.
     */
    default Optional<PresignedUpload> presignUpload(String filename, String contentType, long size) {
        return Optional.empty();
    }
}
