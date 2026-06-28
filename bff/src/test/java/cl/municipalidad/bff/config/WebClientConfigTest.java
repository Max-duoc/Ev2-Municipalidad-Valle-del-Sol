package cl.municipalidad.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientConfigTest {

    @Test
    void beans_deberianCrearWebClientsConBaseUrlConfigurada() {
        WebClientConfig config = new WebClientConfig();
        ReflectionTestUtils.setField(config, "reportesUrl", "http://reportes.local");
        ReflectionTestUtils.setField(config, "monitoreoUrl", "http://monitoreo.local");
        ReflectionTestUtils.setField(config, "usuariosUrl", "http://usuarios.local");

        WebClient reportes = config.reportesClient();
        WebClient monitoreo = config.monitoreoClient();
        WebClient usuarios = config.usuariosClient();

        assertNotNull(reportes);
        assertNotNull(monitoreo);
        assertNotNull(usuarios);
    }
}
