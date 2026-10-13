package basketball.projects.balonAlAire.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.ChangePasswordRequest;
import basketball.projects.balonAlAire.dto.ForgotPasswordRequest;
import basketball.projects.balonAlAire.dto.MessageResponse;
import basketball.projects.balonAlAire.dto.RegisterRequest;
import basketball.projects.balonAlAire.dto.RegisterResponse;
import basketball.projects.balonAlAire.dto.ResetPasswordRequest;
import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.service.PasswordResetService;
import basketball.projects.balonAlAire.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    // Solo ADMIN (SecurityConfig): es el único rol que puede dar de alta usuarios
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
            request.username(),
            request.password(),
            request.email(),
            request.role());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RegisterResponse("Usuario registrado correctamente", user.getUsername(), user.getRole()));
    }

    // SecurityConfig exige sesión para esta ruta, así que principal nunca es null
    @PostMapping("/change-password")
    public MessageResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        userService.changePassword(
            principal.getName(),
            request.currentPassword(),
            request.newPassword());
        return new MessageResponse("Contraseña cambiada correctamente");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.identifier());
        return new MessageResponse("Si existe una cuenta con esos datos, se generó un enlace de recuperación.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return new MessageResponse("Contraseña actualizada correctamente");
    }
}
