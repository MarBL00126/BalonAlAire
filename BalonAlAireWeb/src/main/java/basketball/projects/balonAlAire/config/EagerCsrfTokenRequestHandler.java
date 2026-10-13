package basketball.projects.balonAlAire.config;

import java.util.function.Supplier;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Fuerza a generar el token en cada respuesta para que la cookie XSRF-TOKEN siempre esté disponible. */
class EagerCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        super.handle(request, response, csrfToken);
        csrfToken.get();
    }
}
