package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data 
public class PostRequest {
    @NotEmpty(message = "At least one category is required")
        List<Integer> categoryIds;

        @NotBlank(message = "Title is required")
        String title;

        @NotBlank(message = "Slug is required")
        String slug;

        @NotBlank(message = "Excerpt is required")
        String excerpt;

        @NotBlank(message = "Content is required")
        String content;

        @NotBlank(message = "Image URL is required")
        String imgUrl;

        @NotBlank(message = "Author is required")
        String author;

        PostStatus status;

        LocalDateTime publishedAt;
}
