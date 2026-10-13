package basketball.projects.balonAlAire.config;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CSP como defensa en profundidad contra XSS. El sitio público no usa scripts inline, así que se bloquean;
 * el panel admin sí los usa (handlers onclick), carga TinyMCE desde su CDN y sube archivos directo al storage.
 */
class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    private static final String PUBLIC_POLICY = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' data: https://fonts.gstatic.com",
            "img-src 'self' data: https:",
            "connect-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'self'");

    private final String adminPolicy;

    ContentSecurityPolicyFilter(StorageProperties storage) {
        this.adminPolicy = String.join("; ",
                "default-src 'self'",
                "script-src 'self' 'unsafe-inline' https://cdn.tiny.cloud",
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.tiny.cloud",
                "font-src 'self' data: https://fonts.gstatic.com https://cdn.tiny.cloud",
                "img-src 'self' data: blob: https:",
                "connect-src " + String.join(" ", adminConnectSources(storage)),
                "frame-src 'self' blob:",
                "object-src 'none'",
                "base-uri 'self'",
                "form-action 'self'",
                "frame-ancestors 'self'");
    }

    // Las URLs prefirmadas apuntan al endpoint del storage (con o sin bucket como subdominio)
    private static List<String> adminConnectSources(StorageProperties storage) {
        List<String> sources = new ArrayList<>(List.of("'self'", "blob:", "https://*.tiny.cloud"));
        if (storage.s3Enabled()) {
            URI endpoint = URI.create(storage.endpoint());
            if (endpoint.getScheme() != null && endpoint.getHost() != null) {
                sources.add(endpoint.getScheme() + "://" + endpoint.getHost());
                sources.add(endpoint.getScheme() + "://*." + endpoint.getHost());
            }
        }
        return sources;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean admin = path.equals("/admin") || path.startsWith("/admin/");
        response.setHeader("Content-Security-Policy", admin ? adminPolicy : PUBLIC_POLICY);
        chain.doFilter(request, response);
    }
}
