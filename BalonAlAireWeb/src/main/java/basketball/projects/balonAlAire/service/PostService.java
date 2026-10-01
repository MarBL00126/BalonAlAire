package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.CategoryResponse;
import basketball.projects.balonAlAire.dto.PostRequest;
import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.model.PostStatus;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.repository.CategoryRepository;
import basketball.projects.balonAlAire.repository.PostRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostService(PostRepository postRepository,CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository=categoryRepository;
    }

    public PostResponse getBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return toResponse(post);

    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(this::toResponse);
    }

    public List<PostResponse> getPostsByCategorySlug(String slug) {
        return postRepository.findByCategoriesSlugOrderByPublishedAtDesc(slug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<PostResponse> searchPosts(String search,Pageable pageable) {
        return postRepository
                .fullTextSearch(search, pageable)
                .map(this::toResponse);
    }

    public PostResponse save(PostRequest request) {

        LocalDateTime now = LocalDateTime.now();

        List<Category> categories =
                categoryRepository.findAllById(request.getCategoryIds());

        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setImgUrl(request.getImgUrl());
        post.setAuthor(request.getAuthor());
        post.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : PostStatus.DRAFT
        );
        post.setPublishedAt(request.getPublishedAt());
        post.setCategories(categories);

        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        return toResponse(postRepository.save(post));
    }

    public PostResponse update(Long id, PostRequest request) {

        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"));

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        existing.setTitle(request.getTitle());
        existing.setSlug(request.getSlug());
        existing.setExcerpt(request.getExcerpt());
        existing.setContent(request.getContent());
        existing.setImgUrl(request.getImgUrl());
        existing.setAuthor(request.getAuthor());
        existing.setStatus(request.getStatus());
        existing.setPublishedAt(request.getPublishedAt());
        existing.setCategories(categories);
        existing.setUpdatedAt(LocalDateTime.now());

        return toResponse(postRepository.save(existing));
    }


    public void delete(Long id) {
        postRepository.findById(id)
        .orElseThrow(()->new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found"));

        postRepository.deleteById(id);
    }

    private PostResponse toResponse(Post post) {
        List<CategoryResponse> categories = post.getCategories()
                .stream().map(category -> new CategoryResponse(category.getName(), category.getSlug())).toList();
        return new PostResponse(post.getId(), post.getTitle(), post.getSlug(), post.getExcerpt(),
                post.getContent(), post.getImgUrl(), post.getAuthor(), post.getStatus(), post.getPublishedAt(), categories);
    }
}
