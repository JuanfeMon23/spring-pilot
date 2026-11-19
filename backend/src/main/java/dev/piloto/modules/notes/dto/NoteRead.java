package dev.piloto.modules.notes.dto;

import dev.piloto.modules.notes.domain.Note;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NoteRead(UUID id, String title, String body, boolean isActive, OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static NoteRead from(Note note) {
        return new NoteRead(note.getId(), note.getTitle(), note.getBody(), note.isActive(), note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
