package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;
import java.util.List;

import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.model.PostStatus;

public record PostResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String content,
        String imgUrl,
        String author,
        PostStatus status,
        LocalDateTime publishedAt,
        List<CategoryResponse> categories,
        String downloadText,
        String downloadUrl) {

    // Recorre las categorías LAZY: debe llamarse dentro de una transacción
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getImgUrl(),
                post.getAuthor(),
                post.getStatus(),
                post.getPublishedAt(),
                post.getCategories().stream().map(CategoryResponse::from).toList(),
                post.getDownloadText(),
                post.getDownloadUrl());
    }
}
