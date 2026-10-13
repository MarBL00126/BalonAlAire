package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Acepta usuario o correo, por eso no se valida como email (hay usuarios con espacios y mayúsculas)
public record ForgotPasswordRequest(
        @NotBlank(message = "Ingresá tu usuario o correo")
        @Size(max = 254, message = "El usuario o correo es demasiado largo")
        String identifier) {
}
