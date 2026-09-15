package basketball.projects.balonAlAire.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import basketball.projects.balonAlAire.dto.ChangePasswordRequest;
import basketball.projects.balonAlAire.dto.LoginRequest;
import basketball.projects.balonAlAire.dto.RegisterRequest;
import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authService.login(
                request.getUsername(),
                request.getPassword());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "username", user.getUsername(),
                        "role", user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.getUsername(),
            request.getPassword(),
            request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "message", "Usuario registrado correctamente",
                "username", user.getUsername()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "No autenticado"));
        }
        authService.changePassword(
            principal.getName(),
            request.getCurrentPassword(),
            request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña cambiada correctamente"));
    }
}
