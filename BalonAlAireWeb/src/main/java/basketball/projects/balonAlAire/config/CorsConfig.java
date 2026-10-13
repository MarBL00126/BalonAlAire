package basketball.projects.balonAlAire.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    /**
     * Por defecto solo se permite la URL pública de la app. Otros orígenes (por ejemplo localhost
     * en desarrollo) se agregan explícitamente con ALLOWED_ORIGINS, porque se usa allowCredentials.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
        List<String> configured = properties.cors().allowedOrigins() == null
                ? List.of()
                : properties.cors().allowedOrigins().stream()
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList();
        List<String> origins = configured.isEmpty() ? List.of(properties.publicUrl()) : configured;

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
