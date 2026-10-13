package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.SocialLink;

/** Vista pública: solo los enlaces activos y lo necesario para mostrarlos. */
public record PublicSocialLinkResponse(String platform, String linkUrl) {

    public static PublicSocialLinkResponse from(SocialLink socialLink) {
        return new PublicSocialLinkResponse(socialLink.getPlatform(), socialLink.getLinkUrl());
    }
}
