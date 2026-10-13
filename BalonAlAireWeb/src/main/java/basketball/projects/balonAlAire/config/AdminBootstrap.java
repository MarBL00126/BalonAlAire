package basketball.projects.balonAlAire.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import basketball.projects.balonAlAire.model.Role;
import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.UserRepository;

/**
 * Gestiona el admin inicial desde variables de entorno (ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL)
 * en lugar de credenciales guardadas en migraciones.
 * - Si no hay usuarios, crea el admin.
 * - Si el admin sembrado por V8 conserva la contraseña conocida, la rota a ADMIN_PASSWORD.
 * - Si la contraseña conocida sigue activa y no hay ADMIN_PASSWORD, avisa en el log.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    // Contraseña del usuario sembrado en V8__insert_seed_data.sql (pública en el historial git)
    private static final String SEEDED_PASSWORD = "ChangeMe123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AppProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = properties.admin().username();
        this.password = properties.admin().password();
        this.email = properties.admin().email();
    }

    @Override
    public void run(String... args) {
        boolean hasPassword = password != null && !password.isBlank();

        if (userRepository.count() == 0) {
            if (hasPassword) {
                createAdmin();
            } else {
                log.warn("There are no users and ADMIN_PASSWORD is not set: nobody can log in to the CMS.");
            }
            return;
        }

        userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(SEEDED_PASSWORD, user.getPassword()))
                .ifPresent(user -> {
                    if (hasPassword) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                        log.warn("The seeded password of user '{}' was replaced with ADMIN_PASSWORD.", username);
                    } else {
                        log.error("User '{}' still has the default seeded password. "
                                + "Set ADMIN_PASSWORD (min 8 chars) and restart, or change it from the CMS.",
                                username);
                    }
                });
    }

    private void createAdmin() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        user.setEmail(email == null || email.isBlank() ? null : email.trim().toLowerCase());
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Initial admin user '{}' created.", username);
    }
}
