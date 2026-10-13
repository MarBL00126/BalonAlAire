package basketball.projects.balonAlAire.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Si endpoint está vacío se usa el disco local; si está definido, el resto de los datos de S3 son obligatorios. */
@ConfigurationProperties("storage")
public record StorageProperties(
        String endpoint,
        String publicUrl,
        String accessKey,
        String secretKey,
        @DefaultValue("auto") String region,
        String bucket,
        @DefaultValue("true") boolean pathStyle,
        @DefaultValue("uploads") String localDir) {

    public boolean s3Enabled() {
        return endpoint != null && !endpoint.isBlank();
    }
}
