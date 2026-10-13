package basketball.projects.balonAlAire.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.dto.PublicSocialLinkResponse;
import basketball.projects.balonAlAire.dto.SocialLinkRequest;
import basketball.projects.balonAlAire.dto.SocialLinkResponse;
import basketball.projects.balonAlAire.model.SocialLink;
import basketball.projects.balonAlAire.repository.SocialLinkRepository;

@Service
public class SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }

    @Cacheable("social-links")
    public List<SocialLinkResponse> getAllSocialLinks() {
        return socialLinkRepository.findAll().stream()
                .map(SocialLinkResponse::from)
                .toList();
    }

    @Cacheable("social-links-active")
    public List<PublicSocialLinkResponse> getActiveSocialLinks() {
        return socialLinkRepository.findAllByActiveTrue().stream()
                .map(PublicSocialLinkResponse::from)
                .toList();
    }

    @CacheEvict(value = { "social-links", "social-links-active" }, allEntries = true)
    public SocialLinkResponse createSocialLink(SocialLinkRequest request) {
        SocialLink socialLink = new SocialLink();
        socialLink.setPlatform(request.platform());
        socialLink.setLinkUrl(request.linkUrl());
        socialLink.setActive(request.isActiveOrDefault());
        return SocialLinkResponse.from(socialLinkRepository.save(socialLink));
    }

    @CacheEvict(value = { "social-links", "social-links-active" }, allEntries = true)
    public Optional<SocialLinkResponse> updateSocialLink(Long id, SocialLinkRequest request) {
        return socialLinkRepository.findById(id).map(existing -> {
            existing.setPlatform(request.platform());
            existing.setLinkUrl(request.linkUrl());
            existing.setActive(request.isActiveOrDefault());
            return SocialLinkResponse.from(socialLinkRepository.save(existing));
        });
    }
}
