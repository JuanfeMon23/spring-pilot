package dev.piloto.modules.notes;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotesControllerTest {

    private static final String NOTES_URL = "/api/v1/notes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ResultActions createNote(String title, String body) throws Exception {
        String bodyJson = body == null ? "null" : "\"" + body + "\"";
        return mockMvc.perform(post(NOTES_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + title + "\",\"body\":" + bodyJson + "}"));
    }

    @Test
    void createReturns201WithBodyAndLocation() throws Exception {
        createNote("Comprar leche", "Entera").andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/notes/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Comprar leche"))
                .andExpect(jsonPath("$.body").value("Entera"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void createTrimsTheTitle() throws Exception {
        createNote("  Con espacios  ", null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Con espacios"));
    }

    @Test
    void createWithDuplicatedTitleInOtherCaseReturns409() throws Exception {
        createNote("Comprar leche", null).andExpect(status().isCreated());

        createNote("COMPRAR LECHE", null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("note_title_taken"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details.title").isNotEmpty());
    }

    @Test
    void createWithTitleOfAnInactiveNoteIsAllowed() throws Exception {
        jdbcTemplate.update("INSERT INTO notes (title, is_active) VALUES ('Vieja', false)");

        createNote("vieja", null).andExpect(status().isCreated());
    }

    @Test
    void createWithBlankTitleReturns422() throws Exception {
        createNote("   ", null).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"))
                .andExpect(jsonPath("$.details.title").isNotEmpty());
    }

    @Test
    void createWithoutTitleReturns422() throws Exception {
        mockMvc.perform(post(NOTES_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.details.title").isNotEmpty());
    }

    @Test
    void createWithTitleOver120CharactersReturns422() throws Exception {
        createNote("a".repeat(121), null).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"))
                .andExpect(jsonPath("$.details.title").isNotEmpty());
    }

    @Test
    void createWithTitleOfExactly120CharactersIsAccepted() throws Exception {
        createNote("a".repeat(120), null).andExpect(status().isCreated());
    }

    @Test
    void createWithBodyOver2000CharactersReturns422() throws Exception {
        createNote("Larga", "b".repeat(2001)).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"))
                .andExpect(jsonPath("$.details.body").isNotEmpty());
    }

    @Test
    void listReturnsOnlyActiveNotesNewestFirstWithPagination() throws Exception {
        jdbcTemplate.update("INSERT INTO notes (title, is_active) VALUES ('Inactiva', false)");
        createNote("Primera", null).andExpect(status().isCreated());
        jdbcTemplate.update("UPDATE notes SET created_at = now() - interval '1 hour' WHERE title = 'Primera'");
        createNote("Segunda", null).andExpect(status().isCreated());

        mockMvc.perform(get(NOTES_URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].title").value("Segunda"))
                .andExpect(jsonPath("$.items[1].title").value("Primera"));
    }

    @Test
    void listPaginatesWithPageBaseOne() throws Exception {
        createNote("Nota A", null);
        jdbcTemplate.update("UPDATE notes SET created_at = now() - interval '2 hour' WHERE title = 'Nota A'");
        createNote("Nota B", null);
        jdbcTemplate.update("UPDATE notes SET created_at = now() - interval '1 hour' WHERE title = 'Nota B'");
        createNote("Nota C", null);

        mockMvc.perform(get(NOTES_URL).param("page", "2").param("size", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Nota A"));
    }

    @Test
    void listFiltersByTitleIgnoringCase() throws Exception {
        createNote("Comprar LECHE", null);
        createNote("Pagar luz", null);

        mockMvc.perform(get(NOTES_URL).param("q", "leche")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Comprar LECHE"));
    }

    @Test
    void listTreatsPercentInSearchAsLiteralText() throws Exception {
        createNote("Descuento 50% hoy", null);
        createNote("Otra cosa", null);

        mockMvc.perform(get(NOTES_URL).param("q", "%")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listWithInvalidSizeReturns422() throws Exception {
        mockMvc.perform(get(NOTES_URL).param("size", "101")).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"))
                .andExpect(jsonPath("$.details.size", containsString("100")));
    }
}
