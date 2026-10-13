package basketball.projects.balonAlAire.service;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Reglas de qué archivos se aceptan: tipo, extensión coherente con el tipo y tamaño máximo. */
@Component
public class UploadValidator {

    public static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    public static final long MAX_PDF_SIZE = 100L * 1024 * 1024;

    private static final String PDF = "application/pdf";

    private static final Map<String, Set<String>> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", Set.of(".jpg", ".jpeg"),
            "image/png", Set.of(".png"),
            "image/webp", Set.of(".webp"),
            PDF, Set.of(".pdf"));

    /** Valida el archivo y devuelve su extensión (en minúsculas, con punto). */
    public String validate(String filename, String contentType, long size) {
        Set<String> allowedExtensions = contentType == null ? null : EXTENSIONS_BY_CONTENT_TYPE.get(contentType);
        if (allowedExtensions == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten imágenes JPG, PNG, WebP o archivos PDF");
        }

        String extension = StorageKeys.extensionOf(filename);
        if (!allowedExtensions.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La extensión del archivo no corresponde a su tipo");
        }

        boolean pdf = PDF.equals(contentType);
        if (size > (pdf ? MAX_PDF_SIZE : MAX_IMAGE_SIZE)) {
            throw new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    pdf ? "El PDF no puede superar los 100 MB" : "La imagen no puede superar los 5 MB");
        }
        return extension;
    }
}
