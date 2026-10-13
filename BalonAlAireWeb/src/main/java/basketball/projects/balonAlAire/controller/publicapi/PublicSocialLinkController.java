package basketball.projects.balonAlAire.controller.publicapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.PublicSocialLinkResponse;
import basketball.projects.balonAlAire.service.SocialLinkService;

@RestController
@RequestMapping("/api/public/social-links")
public class PublicSocialLinkController {
    private final SocialLinkService socialLinkService;

    public PublicSocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping
    public List<PublicSocialLinkResponse> getActive() {
        return socialLinkService.getActiveSocialLinks();
    }
}
