package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocialLinkRequest(
        @NotBlank(message = "La plataforma es obligatoria")
        @Size(max = 50, message = "La plataforma no puede superar los 50 caracteres")
        String platform,

        @Pattern(regexp = Urls.HTTP_ONLY, message = Urls.HTTP_ONLY_MESSAGE)
        String linkUrl,

        Boolean active) {

    public boolean isActiveOrDefault() {
        return active == null || active;
    }
}
