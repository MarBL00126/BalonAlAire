package basketball.projects.balonAlAire.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.CurrentUserResponse;

/** Quién es el usuario de la sesión: el panel lo usa para ocultar las secciones que su rol no puede usar. */
@RestController
@RequestMapping("/api/admin/me")
public class AdminMeController {

    @GetMapping
    public CurrentUserResponse me(Authentication authentication) {
        return CurrentUserResponse.from(authentication)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
    }
}
