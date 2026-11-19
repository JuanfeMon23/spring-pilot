package dev.piloto.modules.notes;

import dev.piloto.core.error.AppException;
import dev.piloto.core.error.Conflict;

/** Errores de negocio del módulo de notas. */
final class NotesErrors {

    static final String TITLE_TAKEN_CODE = "note_title_taken";
    static final String UNIQUE_TITLE_INDEX = "uq_notes_title";

    private NotesErrors() {
    }

    static AppException titleTaken() {
        return Conflict.of(TITLE_TAKEN_CODE, "title", "Ya existe una nota activa con ese título.");
    }
}
