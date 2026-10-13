package basketball.projects.balonAlAire;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de las pruebas de integración: Postgres real en un contenedor (Flyway aplica todas las migraciones).
 * Se omiten solas si no hay Docker disponible.
 */
@SpringBootTest(properties = {
        "app.public-url=http://localhost",
        "app.admin.password=",
        "storage.endpoint=",
        "storage.local-dir=target/test-uploads",
        // Valores de relleno: la conexión real la aporta el contenedor (@ServiceConnection)
        "spring.datasource.url=jdbc:postgresql://localhost:5432/unused",
        "spring.datasource.username=unused",
        "spring.datasource.password=unused"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
}
