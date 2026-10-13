package basketball.projects.balonAlAire.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Configuración propia de la app. Si falta algo obligatorio, el arranque falla con el nombre de la propiedad. */
@Validated
@ConfigurationProperties("app")
public record AppProperties(
        @NotBlank(message = "app.public-url (APP_PUBLIC_URL) es obligatorio")
        @Pattern(regexp = "^https?://.+", message = "app.public-url (APP_PUBLIC_URL) debe empezar con http:// o https://")
        String publicUrl,
        @Valid @DefaultValue Cors cors,
        @Valid @DefaultValue Tinymce tinymce,
        @Valid @DefaultValue Admin admin,
        @Valid @DefaultValue Mail mail) {

    /** Orígenes CORS permitidos (ALLOWED_ORIGINS). Vacío: solo la URL pública. */
    public record Cors(List<String> allowedOrigins) {
    }

    public record Tinymce(@DefaultValue("no-api-key") String apiKey) {
    }

    /** Datos del admin inicial (ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL). */
    public record Admin(
            @DefaultValue("admin") String username,
            @Pattern(regexp = "^$|^.{8,}$", message = "app.admin.password (ADMIN_PASSWORD) debe tener al menos 8 caracteres")
            String password,
            String email) {
    }

    public record Mail(String from) {
    }
}
