package dev.piloto.core.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Excepción de negocio con el contrato común de errores: {@code {code, message, details}}.
 * Los módulos la lanzan (directamente o con {@link NotFound} / {@link Conflict}); el
 * {@link ApiExceptionHandler} la traduce a la respuesta HTTP.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Map<String, Object> details;

    public AppException(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = Map.copyOf(details);
    }

    public AppException(HttpStatus status, String code, String message) {
        this(status, code, message, Map.of());
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
