package basketball.projects.balonAlAire.config;

import java.util.LinkedHashMap;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.ForwardedHeaderFilter;

import basketball.projects.balonAlAire.model.Role;
import basketball.projects.balonAlAire.service.LoginAttemptService;

/**
 * Seguridad en tres niveles, de arriba hacia abajo (gana la primera regla que coincide):
 * 1. Público: sitio, API de solo lectura /api/public/** y recuperación de contraseña.
 * 2. ADMIN o EDITOR: noticias, subidas y lo mínimo del panel.
 * 3. Solo ADMIN: todo lo demás de /admin/** y /api/admin/** (categorías, publicidad, configuración, usuarios).
 */
@Configuration
public class SecurityConfig {

        private static final String ADMIN = Role.ADMIN.name();
        private static final String EDITOR = Role.EDITOR.name();

        // --- Nivel 1: público ---

        private static final String[] PUBLIC_SITE = {
                        "/", "/index.html", "/favicon.ico", "/buscar", "/buscar.html", "/noticia", "/noticia.html",
                        "/categorias/**", "/css/**", "/js/**", "/assets/**", "/uploads/**", "/error" };

        private static final String[] PUBLIC_ADMIN_PAGES = {
                        "/admin/login", "/admin/login.html",
                        "/admin/forgot-password", "/admin/forgot-password.html",
                        "/admin/reset-password", "/admin/reset-password.html",
                        "/admin/logout" };

        private static final String[] PUBLIC_AUTH_API = {
                        "/api/auth/forgot-password", "/api/auth/reset-password" };

        private static final String[] HEALTH = { "/actuator/health", "/actuator/health/**" };

        // --- Nivel 2: ADMIN o EDITOR ---

        private static final String[] STAFF_PAGES = {
                        "/admin", "/admin/dashboard.html", "/admin/noticias", "/admin/noticias.html" };

        private static final String[] STAFF_API = {
                        "/api/admin/posts/**", "/api/admin/uploads/**", "/api/admin/config/**", "/api/admin/me",
                        "/api/auth/change-password" };

        // Los editores solo leen las categorías (las necesita el formulario de noticias)
        private static final String STAFF_READ_CATEGORIES = "/api/admin/categories/**";

        // --- Nivel 3: solo ADMIN ---

        private static final String[] ADMIN_ONLY = { "/admin/**", "/api/admin/**", "/api/auth/**" };

        /**
         * Permite que Spring Boot respete el header X-Forwarded-Proto que envía Railway.
         * Sin esto, los redirects generan URLs con http://, causando Mixed Content.
         */
        @Bean
        public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
                FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
                registration.setFilter(new ForwardedHeaderFilter());
                registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
                return registration;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        LoginAttemptService loginAttempts,
                        StorageProperties storageProperties) throws Exception {

                DefaultRedirectStrategy redirects = new DefaultRedirectStrategy();

                http
                                .addFilterAfter(new ContentSecurityPolicyFilter(storageProperties), HeaderWriterFilter.class)
                                .addFilterBefore(new LoginRateLimitFilter(loginAttempts),
                                                UsernamePasswordAuthenticationFilter.class)
                                // Token en cookie legible por JS: fetch lo envía en X-XSRF-TOKEN y los forms en _csrf
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new EagerCsrfTokenRequestHandler()))
                                .cors(Customizer.withDefaults())

                                // La API responde 401 (no redirige al formulario) para que el front pueda distinguirlo del 403;
                                // las páginas del panel siguen redirigiendo al login
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_SITE).permitAll()
                                                .requestMatchers(PUBLIC_ADMIN_PAGES).permitAll()
                                                .requestMatchers(PUBLIC_AUTH_API).permitAll()
                                                .requestMatchers(HEALTH).permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                                                .requestMatchers(STAFF_PAGES).hasAnyRole(ADMIN, EDITOR)
                                                .requestMatchers(STAFF_API).hasAnyRole(ADMIN, EDITOR)
                                                .requestMatchers(HttpMethod.GET, STAFF_READ_CATEGORIES)
                                                .hasAnyRole(ADMIN, EDITOR)

                                                .requestMatchers(ADMIN_ONLY).hasRole(ADMIN)
                                                .anyRequest().hasRole(ADMIN))

                                .formLogin(form -> form
                                                .loginPage("/admin/login")
                                                .loginProcessingUrl("/admin/login")
                                                .successHandler((request, response, authentication) -> {
                                                        loginAttempts.recordLoginSuccess(request.getParameter("username"));
                                                        redirects.sendRedirect(request, response, "/admin");
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        loginAttempts.recordLoginFailure(ClientIp.of(request),
                                                                        request.getParameter("username"));
                                                        redirects.sendRedirect(request, response, "/admin/login?error=true");
                                                })
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/admin/logout")
                                                .logoutSuccessUrl("/admin/login?logout=true")
                                                .permitAll());

                return http.build();
        }

        private static AuthenticationEntryPoint authenticationEntryPoint() {
                LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
                entryPoints.put(request -> request.getRequestURI().startsWith("/api/"),
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                DelegatingAuthenticationEntryPoint delegating = new DelegatingAuthenticationEntryPoint(entryPoints);
                delegating.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/admin/login"));
                return delegating;
        }
}
