package dev.piloto.core.error;

import org.springframework.http.HttpStatus;

/** Fábrica de 404: {@code NotFound.of("note")} produce el código {@code note_not_found}. */
public final class NotFound {

    private NotFound() {
    }

    public static AppException of(String resource) {
        return new AppException(HttpStatus.NOT_FOUND, resource + "_not_found",
                "No se encontró el recurso '" + resource + "'.");
    }
}
