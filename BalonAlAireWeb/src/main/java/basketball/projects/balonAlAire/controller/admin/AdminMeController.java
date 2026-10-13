package basketball.projects.balonAlAire.controller.admin;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.CurrentUserResponse;
import basketball.projects.balonAlAire.model.Role;

/** Quién es el usuario de la sesión: el panel lo usa para ocultar las secciones que su rol no puede usar. */
@RestController
@RequestMapping("/api/admin/me")
public class AdminMeController {

    @GetMapping
    public CurrentUserResponse me(Principal principal, Authentication authentication) {
        Role role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> Role.valueOf(authority.substring("ROLE_".length())))
                .findFirst()
                .orElseThrow();
        return new CurrentUserResponse(principal.getName(), role);
    }
}
