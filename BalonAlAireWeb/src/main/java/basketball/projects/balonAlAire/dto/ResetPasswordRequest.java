package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "El token es obligatorio")
        @Size(max = 200, message = "El token no es válido")
        String token,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = Passwords.MIN_LENGTH, max = 100, message = Passwords.LENGTH_MESSAGE)
        String newPassword) {
}
