package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 100, message = "El slug no puede superar los 100 caracteres")
        @Pattern(regexp = Slugs.PATTERN, message = Slugs.MESSAGE)
        String slug) {
}
