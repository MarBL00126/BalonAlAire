package basketball.projects.balonAlAire.dto;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import basketball.projects.balonAlAire.model.Role;

public record CurrentUserResponse(String username, Role role) {

    /** El usuario de la sesión, o vacío si la petición es anónima. */
    public static Optional<CurrentUserResponse> from(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> Role.valueOf(authority.substring("ROLE_".length())))
                .findFirst()
                .map(role -> new CurrentUserResponse(authentication.getName(), role));
    }
}
