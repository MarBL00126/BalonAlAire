package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotEmpty(message = "Elegí al menos una categoría")
        List<@NotNull(message = "El id de categoría no puede ser nulo") Long> categoryIds,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String title,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 255, message = "El slug no puede superar los 255 caracteres")
        @Pattern(regexp = Slugs.PATTERN, message = Slugs.MESSAGE)
        String slug,

        @NotBlank(message = "El resumen es obligatorio")
        String excerpt,

        @NotBlank(message = "El contenido es obligatorio")
        String content,

        @NotBlank(message = "La imagen es obligatoria")
        @Pattern(regexp = Urls.SITE_OR_HTTP, message = Urls.SITE_OR_HTTP_MESSAGE)
        String imgUrl,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150, message = "El autor no puede superar los 150 caracteres")
        String author,

        PostStatus status,

        LocalDateTime publishedAt,

        @Size(max = 255, message = "El texto de descarga no puede superar los 255 caracteres")
        String downloadText,

        @Size(max = 1000, message = "La URL de descarga no puede superar los 1000 caracteres")
        @Pattern(regexp = Urls.SITE_OR_HTTP, message = Urls.SITE_OR_HTTP_MESSAGE)
        String downloadUrl) {
}
