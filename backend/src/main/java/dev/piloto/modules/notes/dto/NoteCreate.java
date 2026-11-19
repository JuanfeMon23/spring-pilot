package dev.piloto.modules.notes.dto;

import dev.piloto.modules.notes.domain.Note;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreate(
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = Note.TITLE_MAX_LENGTH, message = "El título no puede superar los 120 caracteres.")
        String title,
        @Size(max = Note.BODY_MAX_LENGTH, message = "El cuerpo no puede superar los 2000 caracteres.")
        String body) {

    /** El título se recorta antes de validar: el límite y la unicidad se aplican al valor ya recortado. */
    public NoteCreate {
        title = title == null ? null : title.strip();
    }
}
