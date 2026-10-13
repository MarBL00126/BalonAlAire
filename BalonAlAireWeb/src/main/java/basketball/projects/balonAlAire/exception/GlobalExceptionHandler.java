package basketball.projects.balonAlAire.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);
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
                                "error", "Recurso no encontrado");

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(body);
        }
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationException(
                        MethodArgumentNotValidException exception) {

                Map<String, String> errors=new LinkedHashMap<>();
                exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Valor inválido"));
                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.BAD_REQUEST.value(),
                                "error", "Hay datos inválidos en el formulario",
                                "details",errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(body);
        }
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
                        HttpMessageNotReadableException exception) {

                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.BAD_REQUEST.value(),
                                "error", "El cuerpo de la solicitud no es válido");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(body);
        }
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
                        DataIntegrityViolationException exception) {
                
                log.warn("Database integrity constraint violation", exception);
                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.CONFLICT.value(),
                                "error", "Conflicto de datos (por ejemplo, un slug que ya existe)");

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(body);
        }
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(
                        MaxUploadSizeExceededException exception) {

                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.CONTENT_TOO_LARGE.value(),
                                "error", "El archivo supera el tamaño permitido");

                return ResponseEntity
                                .status(HttpStatus.CONTENT_TOO_LARGE)
                                .body(body);
        }
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(
                        MethodArgumentTypeMismatchException exception) {

                return badRequest("Valor inválido para el parámetro '" + exception.getName() + "'");
        }
        @ExceptionHandler(PropertyReferenceException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidSortProperty(
                        PropertyReferenceException exception) {

                return badRequest("Propiedad de orden inválida: '" + exception.getPropertyName() + "'");
        }
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleException(
                        Exception exception) {

                // Spring MVC errors (405, 415, missing params...) already carry their own status
                if (exception instanceof ErrorResponse errorResponse
                                && errorResponse.getStatusCode().is4xxClientError()) {
                        Map<String, Object> body = Map.of(
                                        "timestamp", LocalDateTime.now(),
                                        "status", errorResponse.getStatusCode().value(),
                                        "error", errorResponse.getBody().getDetail() != null
                                                        ? errorResponse.getBody().getDetail()
                                                        : "Error en la solicitud");
                        return ResponseEntity.status(errorResponse.getStatusCode()).body(body);
                }

                log.error("Unexpected internal server error", exception);
                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "error", "Error interno del servidor");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(body);
        }

        private static ResponseEntity<Map<String, Object>> badRequest(String message) {
                Map<String, Object> body = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.BAD_REQUEST.value(),
                                "error", message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

}
