package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.CategoryResponse;
import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.repository.PostRepository;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostResponse getBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return toResponse(post);

    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByPublishedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<PostResponse> getPostsByCategorySlug(String slug) {
        return postRepository.findByCategoriesSlugOrderByPublishedAtDesc(slug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostResponse> searchPosts(String search) {
        return postRepository
                .findByTitleContainingIgnoreCaseOrExcerptContainingIgnoreCaseOrderByPublishedAtDesc(search, search)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Post save(Post post) {

        LocalDateTime now = LocalDateTime.now();

        if (post.getCreatedAt() == null) {
            post.setCreatedAt(now);
        }

        post.setUpdatedAt(now);

        return postRepository.save(post);
    }

    public Post update(Integer id, Post changes) {

        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"));

        existing.setTitle(changes.getTitle());
        existing.setSlug(changes.getSlug());
        existing.setExcerpt(changes.getExcerpt());
        existing.setContent(changes.getContent());
        existing.setImgUrl(changes.getImgUrl());
        existing.setAuthor(changes.getAuthor());
        existing.setStatus(changes.getStatus());
        existing.setPublishedAt(changes.getPublishedAt());
        existing.setCategories(changes.getCategories());
        existing.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(existing);
    }

    public void delete(Integer id) {

        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found");
        }

        postRepository.deleteById(id);
    }

    private PostResponse toResponse(Post post) {
        List<CategoryResponse> categories = post.getCategories()
                .stream().map(category -> new CategoryResponse(category.getName(), category.getSlug())).toList();
        return new PostResponse(Long.valueOf(post.getId()), post.getTitle(), post.getSlug(), post.getExcerpt(),
                post.getContent(), post.getImgUrl(), post.getAuthor(), post.getStatus(), post.getPublishedAt(), categories);
    }
}
