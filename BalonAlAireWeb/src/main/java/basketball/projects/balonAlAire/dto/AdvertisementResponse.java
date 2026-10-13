package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.Advertisement;

/** Vista pública de un anuncio activo: sin estado ni campos internos. */
public record AdvertisementResponse(
        Long id,
        String name,
        String imageUrl,
        String linkUrl,
        String position) {

    public static AdvertisementResponse from(Advertisement advertisement) {
        return new AdvertisementResponse(
                advertisement.getId(),
                advertisement.getName(),
                advertisement.getImageUrl(),
                advertisement.getLinkUrl(),
                advertisement.getPosition());
    }
}
