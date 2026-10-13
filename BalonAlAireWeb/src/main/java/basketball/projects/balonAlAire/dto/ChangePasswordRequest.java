package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String currentPassword,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = Passwords.MIN_LENGTH, max = 100, message = Passwords.LENGTH_MESSAGE)
        String newPassword) {
}
