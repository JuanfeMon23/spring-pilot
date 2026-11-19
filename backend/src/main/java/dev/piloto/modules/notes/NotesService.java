package dev.piloto.modules.notes;

import dev.piloto.core.web.PageDto;
import dev.piloto.core.web.PageRequestDto;
import dev.piloto.modules.notes.domain.Note;
import dev.piloto.modules.notes.dto.NoteCreate;
import dev.piloto.modules.notes.dto.NoteRead;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class NotesService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final NotesRepository notesRepository;

    NotesService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    PageDto<NoteRead> list(PageRequestDto pageRequest) {
        String search = pageRequest.q() == null ? "" : pageRequest.q().strip();
        var pageable = pageRequest.toPageable(NEWEST_FIRST);
        Page<Note> notes = search.isEmpty()
                ? notesRepository.findActive(pageable)
                : notesRepository.findByIsActiveTrueAndTitleContainingIgnoreCase(search, pageable);
        return PageDto.from(notes, NoteRead::from);
    }

    @Transactional
    NoteRead create(NoteCreate request) {
        if (notesRepository.existsActiveByTitleIgnoreCase(request.title())) {
            throw NotesErrors.titleTaken();
        }
        try {
            return NoteRead.from(notesRepository.saveAndFlush(new Note(request.title(), request.body())));
        } catch (DataIntegrityViolationException violation) {
            if (isTitleIndexViolation(violation)) {
                throw NotesErrors.titleTaken();
            }
            throw violation;
        }
    }

    private static boolean isTitleIndexViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(NotesErrors.UNIQUE_TITLE_INDEX)) {
                return true;
            }
        }
        return false;
    }
}
