package basketball.projects.balonAlAire.controller.publicapi;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.dto.PostSummaryResponse;
import basketball.projects.balonAlAire.service.PostService;

// API pública: solo lectura y solo contenido publicado. SecurityConfig la abre únicamente para GET.
@RestController
@RequestMapping("/api/public/posts")
public class PublicPostController {
    private final PostService postService;

    public PublicPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Page<PostSummaryResponse> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        if (search == null || search.isBlank()) {
            return postService.getAllPosts(pageable);
        }
        return postService.searchPosts(search, pageable);
    }

    @GetMapping("/{slug}")
    public PostResponse getBySlug(@PathVariable String slug) {
        return postService.getBySlug(slug);
    }

    // Noticias relacionadas: las pide la página de detalle (en vez de bajar listados completos)
    @GetMapping("/{slug}/related")
    public List<PostSummaryResponse> getRelated(
            @PathVariable String slug,
            @RequestParam(defaultValue = "3") int limit) {
        return postService.getRelated(slug, limit);
    }
}
