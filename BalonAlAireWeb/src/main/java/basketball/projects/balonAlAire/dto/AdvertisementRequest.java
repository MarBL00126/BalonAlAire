package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data 
public class AdvertisementRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Link URL is required")
    private String linkUrl;

    private String position;

    private boolean active = true;
}
