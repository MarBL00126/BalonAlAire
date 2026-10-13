package basketball.projects.balonAlAire.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.SocialLinkRequest;
import basketball.projects.balonAlAire.dto.SocialLinkResponse;
import basketball.projects.balonAlAire.service.SocialLinkService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/social-links")
public class AdminSocialLinkController {
    private final SocialLinkService socialLinkService;

    public AdminSocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping
    public List<SocialLinkResponse> getAll() {
        return socialLinkService.getAllSocialLinks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SocialLinkResponse create(@Valid @RequestBody SocialLinkRequest request) {
        return socialLinkService.createSocialLink(request);
    }

    @PutMapping("/{id}")
    public SocialLinkResponse update(@PathVariable Long id, @Valid @RequestBody SocialLinkRequest request) {
        return socialLinkService.updateSocialLink(id, request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace social no encontrado"));
    }
}
