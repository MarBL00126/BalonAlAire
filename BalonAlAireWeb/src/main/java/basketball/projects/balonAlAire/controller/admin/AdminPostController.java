package basketball.projects.balonAlAire.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import basketball.projects.balonAlAire.dto.AdminPostSummaryResponse;
import basketball.projects.balonAlAire.dto.PostRequest;
import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.service.PostService;
import jakarta.validation.Valid;

// El acceso por rol está definido en SecurityConfig (/api/admin/**). Aquí sí se ven borradores y programados.
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {
    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    // Tabla del panel: resúmenes, más nuevas primero
    @GetMapping
    public Page<AdminPostSummaryResponse> getAll(@PageableDefault(size = 100) Pageable pageable) {
        return postService.getAllPostsForAdmin(pageable);
    }

    // Noticia completa para editarla
    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable Long id) {
        return postService.getForAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody PostRequest request) {
        return postService.save(request);
    }

    @PutMapping("/{id}")
    public PostResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }
}
