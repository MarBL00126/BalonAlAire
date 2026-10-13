package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.Role;

public record RegisterResponse(String message, String username, Role role) {
}
