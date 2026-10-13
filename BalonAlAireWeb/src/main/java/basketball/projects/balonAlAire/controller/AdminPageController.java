package basketball.projects.balonAlAire.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    @GetMapping
    public String dashboard() {
        return "forward:/admin/dashboard.html";
    }

    // Con sesión activa no tiene sentido mostrar el formulario: va directo al panel
    @GetMapping("/login")
    public String login(Authentication authentication) {
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return loggedIn ? "redirect:/admin" : "forward:/admin/login.html";
    }

    @GetMapping("/noticias")
    public String noticias() {
        return "forward:/admin/noticias.html";
    }

    @GetMapping("/categorias")
    public String categorias() {
        return "forward:/admin/categoria.html";
    }

    @GetMapping("/publicidad")
    public String publicidad() {
        return "forward:/admin/publicidad.html";
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return "forward:/admin/configuracion.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/admin/register.html";
    }
}
