package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.SocialLink;

/** Vista del panel: incluye enlaces inactivos. */
public record SocialLinkResponse(Long id, String platform, String linkUrl, boolean active) {

    public static SocialLinkResponse from(SocialLink socialLink) {
        return new SocialLinkResponse(
                socialLink.getId(),
                socialLink.getPlatform(),
                socialLink.getLinkUrl(),
                socialLink.isActive());
    }
}
