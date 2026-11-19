package dev.piloto.core.db;

import static org.assertj.core.api.Assertions.assertThat;

import dev.piloto.PilotoApplication;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Clean + migrate from scratch on the test database, then boots a fresh JPA context
 * (ddl-auto=validate) against the freshly built schema.
 */
@SpringBootTest
@ActiveProfiles("test")
class MigrationTest {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migratesFromScratchAndJpaContextValidatesTheSchema() {
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        flyway.clean();
        var result = flyway.migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(1);

        try (ConfigurableApplicationContext freshContext = new SpringApplicationBuilder(PilotoApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run()) {
            assertThat(freshContext.isRunning()).isTrue();
        }

        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertThat(tables).contains("notes");
    }
}
