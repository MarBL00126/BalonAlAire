package basketball.projects.balonAlAire.controller;

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

import jakarta.validation.Valid;
import basketball.projects.balonAlAire.model.SocialLink;
import basketball.projects.balonAlAire.service.SocialLinkService;

@RestController
@RequestMapping("/api/social-links")
public class SocialLinkController {
    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping
    public List<SocialLink> getAll() {
        return socialLinkService.getAllSocialLinks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SocialLink create(@Valid @RequestBody SocialLink socialLink) {
        return socialLinkService.createSocialLink(socialLink);
    }

    @PutMapping("/{id}")
    public SocialLink update(@PathVariable Integer id, @Valid @RequestBody SocialLink changes) {
        return socialLinkService.updateSocialLink(id, changes)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Social link not found"));
    }
}
