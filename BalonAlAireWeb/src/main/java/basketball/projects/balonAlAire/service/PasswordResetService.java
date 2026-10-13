package basketball.projects.balonAlAire.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Genera un token temporal para recuperar la contraseña.
     * La respuesta pública no revela si el usuario existe.
     */
    public void requestPasswordReset(String identifier) {
        userService.getByUsernameOrEmail(identifier)
            .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
            .ifPresent(user -> {
                String token = generateResetToken();
                // En la base solo se guarda el hash: el token en claro viaja únicamente por email
                user.setPasswordResetToken(hashToken(token));
                user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
                userRepository.save(user);

                LOGGER.info(
                    "Password reset requested for user '{}'.",
                    user.getUsername());
                emailService.sendPasswordResetEmail(user, token);
            });
    }

    // Un solo commit para cambiar la contraseña y consumir el token; el rechazo por token vencido
    // no debe revertir el borrado de ese token.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(hashToken(token))
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

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private void clearResetToken(User user) {
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }
}
