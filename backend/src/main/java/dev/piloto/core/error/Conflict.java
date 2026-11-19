package dev.piloto.core.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** Fábrica de 409 con el campo en conflicto en {@code details}. */
public final class Conflict {

    private Conflict() {
    }

    public static AppException of(String code, String field, String message) {
        return new AppException(HttpStatus.CONFLICT, code, message, Map.of(field, message));
    }
}
