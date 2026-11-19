package dev.piloto.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigValidationTest {

    @Configuration
    @EnableConfigurationProperties(CorsProperties.class)
    static class PropertiesOnly {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesOnly.class);

    @Test
    void sinOrigenesPermitidosElContextoNoArranca() {
        contextRunner
                .withPropertyValues("app.cors.allowed-methods=GET,POST")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void sinMetodosPermitidosElContextoNoArranca() {
        contextRunner
                .withPropertyValues("app.cors.allowed-origins=http://localhost:4200")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void conTodasLasPropiedadesObligatoriasElContextoArranca() {
        contextRunner
                .withPropertyValues(
                        "app.cors.allowed-origins=http://localhost:4200",
                        "app.cors.allowed-methods=GET,POST")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                            .containsExactly("http://localhost:4200");
                });
    }
}
