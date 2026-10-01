package basketball.projects.balonAlAire.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.dto.SocialLinkRequest;
import basketball.projects.balonAlAire.model.SocialLink;
import basketball.projects.balonAlAire.repository.SocialLinkRepository;

@Service
public class SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }

    @Cacheable("social-links")
    public List<SocialLink> getAllSocialLinks() {
        return socialLinkRepository.findAll();
    }

    @CacheEvict(value = "social-links", allEntries = true)
    public SocialLink createSocialLink(SocialLinkRequest request) {
        SocialLink socialLink = new SocialLink();
        socialLink.setLinkUrl(request.getLinkUrl());
        socialLink.setPlatform(request.getPlatform());
        return socialLinkRepository.save(socialLink);
    }

    @CacheEvict(value = "social-links", allEntries = true)
    public Optional<SocialLink> updateSocialLink(Integer id, SocialLinkRequest request) {
        return socialLinkRepository.findById(id).map(existing -> {
            existing.setPlatform(request.getPlatform());
            existing.setLinkUrl(request.getLinkUrl());
            existing.setActive(request.isActive());
            return socialLinkRepository.save(existing);
        });
    }
}

