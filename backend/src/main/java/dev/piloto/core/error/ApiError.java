package dev.piloto.core.error;

import java.util.Map;

/** Cuerpo de error común a todos los backends del piloto. */
public record ApiError(String code, String message, Map<String, Object> details) {
}
