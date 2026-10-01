package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data 
public class SocialLinkRequest {
    @NotBlank(message = "Platform is required")
    private String platform;

    private String linkUrl;

    private boolean active = true;
}
