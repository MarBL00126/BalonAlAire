package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.model.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String imgUrl;
    private String author;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private List<CategoryResponse> categories;
}
