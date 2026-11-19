package dev.piloto.core.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void notFoundDevuelve404ConCodigoYMensaje() throws Exception {
        mockMvc.perform(get("/probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("note_not_found"))
                .andExpect(jsonPath("$.message").value("No se encontró el recurso 'note'."))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void conflictDevuelve409ConDetallePorCampo() throws Exception {
        mockMvc.perform(get("/probe/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("note_title_duplicate"))
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    void cuerpoInvalidoDevuelve422ConDetallesPorCampo() throws Exception {
        mockMvc.perform(post("/probe/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"body\":\"" + "x".repeat(11) + "\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details.title").value("no puede estar vacío"))
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    void jsonMalformadoDevuelve422RequestValidation() throws Exception {
        mockMvc.perform(post("/probe/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{no es json"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("request_validation"));
    }

    @Test
    void excepcionInesperadaDevuelve500SinTraza() throws Exception {
        mockMvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("internal_error"))
                .andExpect(jsonPath("$.message").value("Error interno del servidor."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secreto-interno"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("at dev.piloto"))));
    }

    record ProbeBody(@NotBlank(message = "no puede estar vacío") String title,
                     @Size(max = 10, message = "máximo 10 caracteres") String body) {
    }

    @Controller
    @RequestMapping("/probe")
    static class ProbeController {

        @org.springframework.web.bind.annotation.GetMapping("/not-found")
        @ResponseBody
        String notFound() {
            throw NotFound.of("note");
        }

        @org.springframework.web.bind.annotation.GetMapping("/conflict")
        @ResponseBody
        String conflict() {
            throw Conflict.of("note_title_duplicate", "title", "Ya existe una nota con ese título.");
        }

        @org.springframework.web.bind.annotation.GetMapping("/boom")
        @ResponseBody
        String boom() {
            throw new IllegalStateException("secreto-interno");
        }

        @PostMapping("/body")
        @ResponseBody
        String body(@Valid @RequestBody ProbeBody probeBody) {
            return probeBody.title();
        }
    }
}
