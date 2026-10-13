package basketball.projects.balonAlAire.controller.publicapi;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.CategoryResponse;
import basketball.projects.balonAlAire.dto.PostSummaryResponse;
import basketball.projects.balonAlAire.service.CategoryService;
import basketball.projects.balonAlAire.service.PostService;

@RestController
@RequestMapping("/api/public/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;
    private final PostService postService;

    public PublicCategoryController(CategoryService categoryService, PostService postService) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{slug}")
    public CategoryResponse getBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug);
    }

    @GetMapping("/{slug}/posts")
    public Page<PostSummaryResponse> getPostsByCategorySlug(
            @PathVariable String slug,
            @PageableDefault(size = 12) Pageable pageable) {
        return postService.getPostsByCategorySlug(slug, pageable);
    }
}
