package basketball.projects.balonAlAire.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Respaldo para desarrollo sin storage externo: guarda en disco local (servido en /uploads). */
public class LocalFileStorageService implements FileStorageService {

    private final Path root;

    public LocalFileStorageService(Path root) {
        this.root = root;
    }

    @Override
    public String upload(MultipartFile file) {
        String key = StorageKeys.newKey(StorageKeys.extensionOf(file.getOriginalFilename()));
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo no válido");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo", e);
        }
        return "/uploads/" + key;
    }
}
