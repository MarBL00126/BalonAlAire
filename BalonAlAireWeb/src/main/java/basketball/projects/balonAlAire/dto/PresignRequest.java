package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PresignRequest(
        @NotBlank(message = "El nombre del archivo es obligatorio")
        String filename,

        @NotBlank(message = "El tipo de archivo es obligatorio")
        String contentType,

        @Positive(message = "El tamaño del archivo debe ser mayor a cero")
        long size) {
}
