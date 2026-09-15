package basketball.projects.balonAlAire.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * Serves the static HTML pages for frontend routes.
 * Spring Boot only serves files from /static for exact paths; this controller
 * maps the "clean" URLs the frontend uses to the corresponding HTML files.
 */
@RestController
public class PageController {

    @GetMapping(value = "/noticia", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> noticia() {
        Resource resource = new ClassPathResource("static/noticia.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }

    @GetMapping(value = "/buscar", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> buscar() {
        Resource resource = new ClassPathResource("static/buscar.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }

    @GetMapping(value = "/categorias/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> categoria(@PathVariable String slug) {
        Resource resource = new ClassPathResource("static/categoria.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }
}
