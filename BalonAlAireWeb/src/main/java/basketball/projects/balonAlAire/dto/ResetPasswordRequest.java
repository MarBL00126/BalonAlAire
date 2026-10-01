package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Necessary token")
    private String token;

    @NotBlank(message = "Necessary password")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String newPassword;
}
