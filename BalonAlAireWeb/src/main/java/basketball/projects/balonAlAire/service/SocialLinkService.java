package basketball.projects.balonAlAire.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.model.SocialLink;
import basketball.projects.balonAlAire.repository.SocialLinkRepository;

@Service
public class SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }

    public List<SocialLink> getAllSocialLinks() {
        return socialLinkRepository.findAll();
    }

    public SocialLink createSocialLink(SocialLink socialLink) {
        return socialLinkRepository.save(socialLink);
    }

    public Optional<SocialLink> updateSocialLink(Integer id, SocialLink changes) {
        return socialLinkRepository.findById(id).map(existing -> {
            existing.setPlatform(changes.getPlatform());
            existing.setLinkUrl(changes.getLinkUrl());
            existing.setActive(changes.isActive());
            return socialLinkRepository.save(existing);
        });
    }
}
