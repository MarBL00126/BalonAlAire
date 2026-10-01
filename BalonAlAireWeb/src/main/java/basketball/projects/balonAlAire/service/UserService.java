package basketball.projects.balonAlAire.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /** Busca un usuario por username. Devuelve Optional vacío si no existe. */
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /** Busca un usuario por username o email. */
    public Optional<User> getByUsernameOrEmail(String identifier) {
        String normalized = identifier.trim();
        return userRepository.findByUsername(normalized)
            .or(() -> userRepository.findByEmailIgnoreCase(normalized));
    }

    /**
     * Registra un nuevo usuario admin.
     * Lanza CONFLICT si el username ya existe.
     */
    public User register(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El nombre de usuario ya existe");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo electrónico ya está asociado a otro usuario");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("ADMIN");
        user.setEmail(normalizedEmail);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Cambia la contraseña de un usuario autenticado.
     * Verifica que la contraseña actual sea correcta antes de actualizar.
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Genera un token temporal para recuperar contraseña.
     * La respuesta pública no revela si el usuario existe.
     */
    public void requestPasswordReset(String identifier) {
        getByUsernameOrEmail(identifier)
            .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
            .ifPresent(user -> {
                String token = generateResetToken();
                user.setPasswordResetToken(token);
                user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
                userRepository.save(user);

                LOGGER.info(
                    "Password reset requested for user '{}'.",
                    user.getUsername());
                emailService.sendPasswordResetEmail(user, token);
            });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El enlace de recuperación no es válido o ya fue utilizado"));

        LocalDateTime expiresAt = user.getPasswordResetExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            clearResetToken(user);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El enlace de recuperación expiró");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        clearResetToken(user);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    private void clearResetToken(User user) {
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }

    /**
     * Implementación de UserDetailsService para Spring Security.
     * Permite que SecurityConfig inyecte UserService en lugar de UserRepository.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsernameOrEmail(username)
            .map(user -> org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

