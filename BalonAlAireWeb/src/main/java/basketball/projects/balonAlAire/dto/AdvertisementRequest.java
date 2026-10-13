package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdvertisementRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @NotBlank(message = "La imagen es obligatoria")
        @Pattern(regexp = Urls.SITE_OR_HTTP, message = Urls.SITE_OR_HTTP_MESSAGE)
        String imageUrl,

        @NotBlank(message = "El enlace es obligatorio")
        @Pattern(regexp = Urls.HTTP_ONLY, message = Urls.HTTP_ONLY_MESSAGE)
        String linkUrl,

        @Size(max = 100, message = "La posición no puede superar los 100 caracteres")
        String position,

        Boolean active) {

    public boolean isActiveOrDefault() {
        return active == null || active;
    }
}
