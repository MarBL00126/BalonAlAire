package basketball.projects.balonAlAire.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.service.PostService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> getAll(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) {
            return postService.getAllPosts();
        }
        return postService.searchPosts(search);
    }

    @GetMapping("/{slug}")
    public PostResponse getBySlug(@PathVariable String slug) {
        return postService.getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@Valid @RequestBody Post post) {
        return postService.save(post);
    }

    @PutMapping("/{id}")
    public Post update(
            @PathVariable Integer id,
            @Valid @RequestBody Post post) {

        return postService.update(id, post);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        postService.delete(id);
    }

}
