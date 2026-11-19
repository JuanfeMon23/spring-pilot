package dev.piloto.core.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.piloto.modules.notes.domain.Note;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotesSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private void insertNote(String title, boolean isActive) {
        jdbcTemplate.update("INSERT INTO notes (title, is_active) VALUES (?, ?)", title, isActive);
    }

    @Test
    void duplicateTitleAmongActiveNotesIgnoringCaseViolatesUniqueIndex() {
        insertNote("Comprar leche", true);

        assertThatThrownBy(() -> insertNote("COMPRAR LECHE", true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_notes_title");
    }

    @Test
    void duplicateTitleIsAllowedWhenThePreviousNoteIsInactive() {
        insertNote("Comprar leche", true);
        jdbcTemplate.update("UPDATE notes SET is_active = false WHERE lower(title) = 'comprar leche'");

        insertNote("comprar leche", true);

        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM notes WHERE lower(title) = 'comprar leche' AND is_active", Integer.class);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void blankTitleViolatesLengthCheck() {
        assertThatThrownBy(() -> insertNote("   ", true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_notes_title_length");
    }

    @Test
    void emptyTitleViolatesLengthCheck() {
        assertThatThrownBy(() -> insertNote("", true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_notes_title_length");
    }

    @Test
    void databaseGeneratesIdAndTimestampsOnInsert() {
        insertNote("Nota con defaults", true);

        var row = jdbcTemplate.queryForMap(
                "SELECT id, is_active, body, created_at, updated_at FROM notes WHERE title = 'Nota con defaults'");

        assertThat(row.get("id")).isInstanceOf(UUID.class);
        assertThat(row.get("is_active")).isEqualTo(true);
        assertThat(row.get("body")).isNull();
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isEqualTo(row.get("created_at"));
    }

    @Test
    void updateRefreshesUpdatedAtButKeepsCreatedAt() {
        insertNote("Nota a editar", true);
        Timestamp createdAt = jdbcTemplate.queryForObject(
                "SELECT created_at FROM notes WHERE title = 'Nota a editar'", Timestamp.class);

        jdbcTemplate.update("UPDATE notes SET body = 'editada' WHERE title = 'Nota a editar'");

        var row = jdbcTemplate.queryForMap(
                "SELECT created_at, updated_at FROM notes WHERE title = 'Nota a editar'");
        assertThat(row.get("created_at")).isEqualTo(createdAt);
        assertThat((Timestamp) row.get("updated_at")).isAfter((Timestamp) row.get("created_at"));
    }

    @Test
    void persistingTheEntityLeavesIdAndTimestampsToTheDatabase() {
        Note note = new Note("Nota desde JPA", "cuerpo");

        entityManager.persist(note);
        entityManager.flush();

        assertThat(note.getId()).isNotNull();
        assertThat(note.getCreatedAt()).isNotNull();
        assertThat(note.getUpdatedAt()).isNotNull();
        assertThat(note.isActive()).isTrue();
    }
}
