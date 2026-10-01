package basketball.projects.balonAlAire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data 
public class CategoryRequest {
    @NotBlank(message = "Name is required")
        String name;

        @NotBlank(message = "Slug is required")
        String slug;
}
