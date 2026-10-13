package basketball.projects.balonAlAire.config;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import basketball.projects.balonAlAire.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Corta con anticipación los intentos de login bloqueados y limita los pedidos de recuperación de contraseña. */
class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    LoginRateLimitFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        if (HttpMethod.POST.matches(request.getMethod())) {
            String path = request.getRequestURI();
            String ip = ClientIp.of(request);

            if ("/admin/login".equals(path)
                    && attempts.isLoginBlocked(ip, request.getParameter("username"))) {
                response.sendRedirect("/admin/login?locked=true");
                return;
            }

            if ("/api/auth/forgot-password".equals(path) && !attempts.allowPasswordResetRequest(ip)) {
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(LoginAttemptService.WINDOW.toSeconds()));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Demasiados intentos. Probá nuevamente en unos minutos.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
