package basketball.projects.balonAlAire.dto;

import java.time.LocalDateTime;

import basketball.projects.balonAlAire.model.Advertisement;

/** Vista del panel: incluye estado y fechas. */
public record AdvertisementAdminResponse(
        Long id,
        String name,
        String imageUrl,
        String linkUrl,
        String position,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static AdvertisementAdminResponse from(Advertisement advertisement) {
        return new AdvertisementAdminResponse(
                advertisement.getId(),
                advertisement.getName(),
                advertisement.getImageUrl(),
                advertisement.getLinkUrl(),
                advertisement.getPosition(),
                advertisement.isActive(),
                advertisement.getCreatedAt(),
                advertisement.getUpdatedAt());
    }
}
