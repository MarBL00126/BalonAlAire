package basketball.projects.balonAlAire.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** Storage S3 compatible (S3, R2, MinIO). Lo crea StorageConfig; Spring cierra los clientes al apagar la app. */
public class S3FileStorageService implements FileStorageService, AutoCloseable {

    private static final Duration PRESIGN_VALIDITY = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucketName;
    private final String publicUrl;

    public S3FileStorageService(S3Client s3Client, S3Presigner presigner, String bucketName, String publicUrl) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    @Override
    public String upload(MultipartFile file) {
        String key = StorageKeys.newKey(StorageKeys.extensionOf(file.getOriginalFilename()));

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return publicUrl + "/" + key;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer el archivo", e);
        } catch (S3Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo", e);
        }
    }

    // El tipo y el tamaño quedan firmados: el storage rechaza un PUT que no coincida con lo validado
    @Override
    public Optional<PresignedUpload> presignUpload(String filename, String contentType, long size) {
        String key = StorageKeys.newKey(StorageKeys.extensionOf(filename));

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        String uploadUrl = presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGN_VALIDITY)
                        .putObjectRequest(request)
                        .build())
                .url()
                .toString();

        return Optional.of(new PresignedUpload(uploadUrl, publicUrl + "/" + key, Map.of("Content-Type", contentType)));
    }

    @Override
    public void close() {
        presigner.close();
        s3Client.close();
    }
}
