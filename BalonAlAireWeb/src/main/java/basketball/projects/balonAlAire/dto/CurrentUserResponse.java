package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.Role;

public record CurrentUserResponse(String username, Role role) {
}
