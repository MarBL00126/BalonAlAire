package basketball.projects.balonAlAire.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private static final Path UPLOAD_DIR = Paths.get("uploads", "ads")
            .toAbsolutePath()
            .normalize();

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(
            @RequestParam("file") MultipartFile file) {

        /*
         * ====================================================
         * VALIDACIÓN BÁSICA
         * ====================================================
         */

        if (file == null || file.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se recibió ningún archivo");
        }

        /*
         * ====================================================
         * VALIDAR TAMAÑO
         * ====================================================
         */

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "La imagen no puede superar los 5 MB");
        }

        /*
         * ====================================================
         * VALIDAR CONTENT TYPE
         * ====================================================
         */

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten imágenes JPG, PNG o WebP");
        }

        /*
         * ====================================================
         * EXTENSIÓN
         * ====================================================
         */

        String extension = getExtension(file.getOriginalFilename());

        /*
         * ====================================================
         * GENERAR NOMBRE ÚNICO
         * ====================================================
         */

        String filename = UUID.randomUUID()
                .toString()
                + extension;

        /*
         * ====================================================
         * CREAR DIRECTORIO
         * ====================================================
         */

        try {

            Files.createDirectories(
                    UPLOAD_DIR);

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo crear el directorio de uploads",
                    e);
        }

        /*
         * ====================================================
         * GUARDAR ARCHIVO
         * ====================================================
         */

        Path destination = UPLOAD_DIR.resolve(filename)
                .normalize();

        /*
         * Seguridad adicional:
         * impedir que el path escape de uploads/ads.
         */

        if (!destination.startsWith(UPLOAD_DIR)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de archivo inválido");
        }

        try {

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen",
                    e);
        }

        /*
         * ====================================================
         * DEVOLVER URL
         * ====================================================
         */

        String url = "/uploads/ads/" + filename;

        return Map.of(
                "url",
                url);
    }

    private String getExtension(
            String originalFilename) {

        if (originalFilename == null ||
                !originalFilename.contains(".")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo no tiene una extensión válida");
        }

        String extension = originalFilename
                .substring(
                        originalFilename.lastIndexOf("."))
                .toLowerCase();

        if (!extension.equals(".jpg") &&
                !extension.equals(".jpeg") &&
                !extension.equals(".png") &&
                !extension.equals(".webp")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Extensión de imagen no permitida");
        }

        return extension;
    }
}
