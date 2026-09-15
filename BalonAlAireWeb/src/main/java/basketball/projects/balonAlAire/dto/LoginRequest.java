package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Necessary username")
    private String username;
    @NotBlank(message = "Necessary password")
    private String password;
}
