package basketball.projects.balonAlAire.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        // most specific handler wins: keeps the real status (404, 401, etc.) instead of
        // forcing one
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(
                        ResponseStatusException exception) {

                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", exception.getStatusCode().value(),
                                "error",
                                exception.getReason() != null ? exception.getReason() : exception.getMessage());

                return ResponseEntity
                                .status(exception.getStatusCode())
                                .body(body);
        }

        // missing static files (e.g. imagenes/ads inexistentes) deben seguir siendo
        // 404, no 500
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(
                        NoResourceFoundException exception) {

                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.NOT_FOUND.value(),
                                "error", "Resource not found");

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(body);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleException(
                        Exception exception) {

                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "error", "Internal server error");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(body);
        }

}
