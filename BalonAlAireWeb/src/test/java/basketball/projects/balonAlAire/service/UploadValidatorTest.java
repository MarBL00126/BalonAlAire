package basketball.projects.balonAlAire.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UploadValidatorTest {

    private final UploadValidator validator = new UploadValidator();

    @Test
    void acceptsMatchingTypeAndExtension() {
        assertThat(validator.validate("foto.JPG", "image/jpeg", 1024)).isEqualTo(".jpg");
        assertThat(validator.validate("nota.pdf", "application/pdf", 50L * 1024 * 1024)).isEqualTo(".pdf");
    }

    @Test
    void rejectsUnknownContentType() {
        assertThatThrownBy(() -> validator.validate("a.svg", "image/svg+xml", 10))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsExtensionThatDoesNotMatchTheContentType() {
        assertThatThrownBy(() -> validator.validate("script.html", "image/png", 10))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> validator.validate("sin-extension", "image/png", 10))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void rejectsFilesOverTheLimit() {
        assertThatThrownBy(() -> validator.validate("foto.png", "image/png", UploadValidator.MAX_IMAGE_SIZE + 1))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE));
        assertThatThrownBy(() -> validator.validate("doc.pdf", "application/pdf", UploadValidator.MAX_PDF_SIZE + 1))
                .isInstanceOf(ResponseStatusException.class);
    }
}
