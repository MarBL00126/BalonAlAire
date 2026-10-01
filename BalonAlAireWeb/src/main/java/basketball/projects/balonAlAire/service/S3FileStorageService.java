package basketball.projects.balonAlAire.service;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@ConditionalOnBean(S3Client.class)
public class S3FileStorageService implements FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl; 

    public S3FileStorageService(
            S3Client s3Client,
            @Value ("${storage.bucket}") String bucketName,
        @Value("${storage.public-url}") String publicUrl) {

        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicUrl=publicUrl;
    }
    @Override
    public String upload(MultipartFile file) {

        String extension = getExtension(file.getOriginalFilename());

        String filename = UUID.randomUUID() + extension;

        String key = "ads/" + filename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return publicUrl + "/" + key;

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo leer la imagen",
                    e
            );

        } catch (S3Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen",
                    e
            );
        }
    }

    private String getExtension(String originalFilename) {

        if (originalFilename == null ||
                !originalFilename.contains(".")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo no tiene una extensión válida"
            );
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Extensión de imagen no permitida"
            );
        }

        return extension;
    }

}
