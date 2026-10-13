package basketball.projects.balonAlAire.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import basketball.projects.balonAlAire.service.FileStorageService;
import basketball.projects.balonAlAire.service.LocalFileStorageService;
import basketball.projects.balonAlAire.service.S3FileStorageService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {

    // Una sola decisión S3 vs local, tomada acá y no con condiciones sobre beans entre clases escaneadas
    @Bean
    public FileStorageService fileStorageService(StorageProperties properties) {
        if (!properties.s3Enabled()) {
            return new LocalFileStorageService(Path.of(properties.localDir()).toAbsolutePath().normalize());
        }
        requireS3Settings(properties);

        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        Region region = Region.of(properties.region());
        URI endpoint = URI.create(properties.endpoint());
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(properties.pathStyle())
                .build();

        S3Client client = S3Client.builder()
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .region(region)
                .serviceConfiguration(s3Configuration)
                .build();
        S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .region(region)
                .serviceConfiguration(s3Configuration)
                .build();

        return new S3FileStorageService(client, presigner, properties.bucket(), properties.publicUrl());
    }

    private static void requireS3Settings(StorageProperties properties) {
        Map<String, String> required = new LinkedHashMap<>();
        required.put("STORAGE_PUBLIC_URL", properties.publicUrl());
        required.put("STORAGE_ACCESS_KEY", properties.accessKey());
        required.put("STORAGE_SECRET_KEY", properties.secretKey());
        required.put("STORAGE_BUCKET", properties.bucket());

        String missing = required.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().isBlank())
                .map(Map.Entry::getKey)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
        if (missing != null) {
            throw new IllegalStateException(
                    "STORAGE_ENDPOINT está definido pero faltan variables de S3: " + missing);
        }
    }
}
