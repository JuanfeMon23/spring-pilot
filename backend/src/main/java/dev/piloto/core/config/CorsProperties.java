package dev.piloto.core.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** CORS explícito: orígenes y métodos son obligatorios (si faltan, la aplicación no arranca). */
@Validated
@ConfigurationProperties("app.cors")
public record CorsProperties(
        @NotEmpty List<String> allowedOrigins,
        @NotEmpty List<String> allowedMethods,
        @DefaultValue({"Content-Type", "Accept"}) List<String> allowedHeaders,
        @DefaultValue("false") boolean allowCredentials) {
}
