package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.Category;

public record CategoryResponse(Long id, String name, String slug) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }
}
