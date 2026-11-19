package dev.piloto.modules.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.piloto.core.error.AppException;
import dev.piloto.modules.notes.domain.Note;
import dev.piloto.modules.notes.dto.NoteCreate;
import dev.piloto.modules.notes.dto.NoteRead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock
    private NotesRepository notesRepository;

    private NotesService notesService;

    @BeforeEach
    void createService() {
        notesService = new NotesService(notesRepository);
    }

    @Test
    void createWithTitleAlreadyTakenThrowsConflictWithoutInserting() {
        when(notesRepository.existsActiveByTitleIgnoreCase("Comprar leche")).thenReturn(true);

        assertThatThrownBy(() -> notesService.create(new NoteCreate("Comprar leche", null)))
                .isInstanceOfSatisfying(AppException.class, error -> {
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(error.getCode()).isEqualTo("note_title_taken");
                    assertThat(error.getDetails()).containsKey("title");
                });
        verify(notesRepository, never()).saveAndFlush(any());
    }

    @Test
    void createTranslatesUniqueIndexViolationFromARaceIntoConflict() {
        when(notesRepository.existsActiveByTitleIgnoreCase("Carrera")).thenReturn(false);
        when(notesRepository.saveAndFlush(any(Note.class))).thenThrow(new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_notes_title\""));

        assertThatThrownBy(() -> notesService.create(new NoteCreate("Carrera", null)))
                .isInstanceOfSatisfying(AppException.class, error -> {
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(error.getCode()).isEqualTo("note_title_taken");
                });
    }

    @Test
    void createDoesNotHideOtherIntegrityViolations() {
        when(notesRepository.existsActiveByTitleIgnoreCase("Otra")).thenReturn(false);
        DataIntegrityViolationException otherViolation = new DataIntegrityViolationException("ck_notes_title_length");
        when(notesRepository.saveAndFlush(any(Note.class))).thenThrow(otherViolation);

        assertThatThrownBy(() -> notesService.create(new NoteCreate("Otra", null))).isSameAs(otherViolation);
    }

    @Test
    void createPersistsTheNoteAndReturnsItsRead() {
        when(notesRepository.existsActiveByTitleIgnoreCase("Nueva")).thenReturn(false);
        when(notesRepository.saveAndFlush(any(Note.class))).thenAnswer(call -> call.getArgument(0));

        NoteRead created = notesService.create(new NoteCreate("Nueva", "cuerpo"));

        assertThat(created.title()).isEqualTo("Nueva");
        assertThat(created.body()).isEqualTo("cuerpo");
        assertThat(created.isActive()).isTrue();
    }
}
