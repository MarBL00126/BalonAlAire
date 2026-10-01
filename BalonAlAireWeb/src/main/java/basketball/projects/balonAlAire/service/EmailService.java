package basketball.projects.balonAlAire.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.model.User;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String publicUrl;
    private final String from;
    private final String mailHost;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.public-url:http://localhost:8080}") String publicUrl,
            @Value("${app.mail.from:}") String from,
            @Value("${spring.mail.host:}") String mailHost) {
        this.mailSenderProvider = mailSenderProvider;
        this.publicUrl = publicUrl;
        this.from = from;
        this.mailHost = mailHost;
    }

    public void sendPasswordResetEmail(User user, String token) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            LOGGER.warn("Password reset requested for user '{}' but no email is configured.", user.getUsername());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || mailHost == null || mailHost.isBlank()) {
            LOGGER.error(
                "Password reset email could not be sent because SMTP is not configured. User: '{}'",
                user.getUsername());
            return;
        }

        String resetUrl = buildResetUrl(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setSubject("Recuperar contraseña - Balón Al Aire");
        message.setText("""
                Hola,

                Recibimos una solicitud para recuperar la contraseña del panel de Balón Al Aire.

                Abrí este enlace para crear una nueva contraseña:
                %s

                El enlace vence en 30 minutos. Si no pediste este cambio, podés ignorar este mensaje.
                """.formatted(resetUrl));

        try {
            mailSender.send(message);
            LOGGER.info("Password reset email sent to user '{}'.", user.getUsername());
        } catch (MailException ex) {
            LOGGER.error("Password reset email could not be sent to user '{}'.", user.getUsername(), ex);
        }
    }

    private String buildResetUrl(String token) {
        String baseUrl = publicUrl.endsWith("/")
            ? publicUrl.substring(0, publicUrl.length() - 1)
            : publicUrl;
        return baseUrl
            + "/admin/reset-password.html?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
