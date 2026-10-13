package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.model.Role;
import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Busca un usuario por username o email. */
    public Optional<User> getByUsernameOrEmail(String identifier) {
        String normalized = identifier.trim();
        return userRepository.findByUsername(normalized)
            .or(() -> userRepository.findByEmailIgnoreCase(normalized));
    }

    /** Registra un usuario del panel con el rol indicado. Lanza CONFLICT si el usuario o el correo ya existen. */
    public User register(String username, String rawPassword, String email, Role role) {
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
        user.setRole(role);
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
}
