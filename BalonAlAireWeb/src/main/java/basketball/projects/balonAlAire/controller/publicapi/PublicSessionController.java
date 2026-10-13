package basketball.projects.balonAlAire.controller.publicapi;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.CurrentUserResponse;

/** Permite al sitio público saber si quien lo mira tiene sesión del panel (solo revela la identidad de la propia sesión). */
@RestController
@RequestMapping("/api/public/session")
public class PublicSessionController {

    @GetMapping
    public ResponseEntity<CurrentUserResponse> current(Authentication authentication) {
        return CurrentUserResponse.from(authentication)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
