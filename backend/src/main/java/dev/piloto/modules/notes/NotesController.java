package dev.piloto.modules.notes;

import dev.piloto.core.web.PageDto;
import dev.piloto.core.web.PageRequestDto;
import dev.piloto.modules.notes.dto.NoteCreate;
import dev.piloto.modules.notes.dto.NoteRead;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/notes")
class NotesController {

    private final NotesService notesService;

    NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping
    PageDto<NoteRead> list(@Valid PageRequestDto pageRequest) {
        return notesService.list(pageRequest);
    }

    @PostMapping
    ResponseEntity<NoteRead> create(@Valid @RequestBody NoteCreate request) {
        NoteRead created = notesService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
