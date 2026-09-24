package basketball.projects.balonAlAire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.ForwardedHeaderFilter;

import basketball.projects.balonAlAire.repository.UserRepository;

@Configuration
public class SecurityConfig {

        /**
         * Permite que Spring Boot respete el header X-Forwarded-Proto que env�a Railway.
         * Sin esto, los redirects generan URLs con http://, causando Mixed Content.
         */
        @Bean
        public ForwardedHeaderFilter forwardedHeaderFilter() {
                return new ForwardedHeaderFilter();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())

                                .authorizeHttpRequests(auth -> auth

                                                // =====================================
                                                // FRONTEND PÚBLICO
                                                // =====================================

                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/buscar",
                                                                "/buscar.html",
                                                                "/noticia",
                                                                "/noticia.html",
                                                                "/categorias/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/assets/**")
                                                .permitAll()

                                                // =====================================
                                                // IMÁGENES SUBIDAS
                                                // =====================================

                                                .requestMatchers("/uploads/**")
                                                .permitAll()

                                                // =====================================
                                                // PANEL ADMINISTRATIVO
                                                // =====================================

                                                .requestMatchers(
                                                                "/admin/login",
                                                                "/admin/login.html",
                                                                "/admin/register",
                                                                "/admin/register.html",
                                                                "/css/admin.css",
                                                                "/js/admin/login.js")
                                                .permitAll()

                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                // =====================================
                                                // LOGIN API
                                                // =====================================

                                                .requestMatchers("/api/auth/login", "/api/auth/register")
                                                .permitAll()

                                                // =====================================
                                                // UPLOADS
                                                // =====================================

                                                .requestMatchers("/api/uploads/**")
                                                .hasRole("ADMIN")

                                                // =====================================
                                                // GET PÚBLICOS
                                                // =====================================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/posts/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/categories/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/advertisements/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/social-links/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/settings/**")
                                                .permitAll()

                                                // =====================================
                                                // OPERACIONES ADMINISTRATIVAS
                                                // =====================================

                                                .requestMatchers("/api/posts/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/categories/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/advertisements/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/social-links/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/settings/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/auth/change-password")
                                                .hasRole("ADMIN")

                                                // =====================================
                                                // RESTO
                                                // =====================================

                                                .anyRequest()
                                                .authenticated())

                                // =====================================
                                // LOGIN WEB DEL CMS
                                // =====================================

                                .formLogin(form -> form
                                                .loginPage("/admin/login")
                                                .loginProcessingUrl("/admin/login")
                                                .defaultSuccessUrl("/admin", true)
                                                .failureUrl("/admin/login?error=true")
                                                .permitAll())

                                // =====================================
                                // LOGOUT
                                // =====================================

                                .logout(logout -> logout
                                                .logoutUrl("/admin/logout")
                                                .logoutSuccessUrl("/admin/login?logout=true")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository) {

                return username -> userRepository.findByUsername(username)
                                .map(user -> User.withUsername(user.getUsername())
                                                .password(user.getPassword())
                                                .roles(user.getRole())
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

}
