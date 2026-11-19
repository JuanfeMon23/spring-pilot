package dev.piloto.modules.notes;

import dev.piloto.modules.notes.domain.Note;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotesRepository extends JpaRepository<Note, UUID> {

    @Query("select note from Note note where note.isActive = true")
    Page<Note> findActive(Pageable pageable);

    /** Derivada: Spring Data escapa los comodines LIKE del texto buscado. */
    Page<Note> findByIsActiveTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("select count(note) > 0 from Note note where note.isActive = true and lower(note.title) = lower(:title)")
    boolean existsActiveByTitleIgnoreCase(@Param("title") String title);
}
