package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = Passwords.MIN_LENGTH, max = 100, message = Passwords.LENGTH_MESSAGE)
        String password,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Role role) {
}
