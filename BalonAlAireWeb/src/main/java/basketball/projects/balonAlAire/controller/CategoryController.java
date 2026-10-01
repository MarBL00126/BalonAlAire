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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.CategoryRequest;
import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.service.CategoryService;
import basketball.projects.balonAlAire.service.PostService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final PostService postService;

    public CategoryController(CategoryService categoryService,PostService postService) {
        this.categoryService = categoryService;
        this.postService=postService;
    }

    @GetMapping
    public List<Category> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{slug}")
    public Category getBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug);
    }

    @GetMapping("/{slug}/posts")
    public List<PostResponse> getPostsByCategorySlug(@PathVariable String slug) {
        return postService.getPostsByCategorySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.save(request);
    }

    @PutMapping("/{id}")
    public Category update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {

        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        categoryService.delete(id);
    }
}
