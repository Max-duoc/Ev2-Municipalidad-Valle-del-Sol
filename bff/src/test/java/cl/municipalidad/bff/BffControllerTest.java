package cl.municipalidad.bff;

import cl.municipalidad.bff.controller.BffController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BffControllerTest {

    private final BffController bffController = new BffController(null, null);

    @Test
    void fallbackFocosActivos_deberiaRetornarServiceUnavailable() {
        ResponseEntity<?> response = bffController.fallbackFocosActivos(new RuntimeException("Timeout"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("CIRCUIT_OPEN", body.get("status"));
    }

    @Test
    void fallbackRegistrarFoco_deberiaRetornarServiceUnavailable() {
        ResponseEntity<?> response = bffController.fallbackRegistrarFoco(
                Map.of("tipo", "FORESTAL"),
                new RuntimeException("Connection refused")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("CIRCUIT_OPEN", body.get("status"));
    }

    @Test
    void fallbackActualizarFoco_deberiaIncluirIdEnMensaje() {
        ResponseEntity<?> response = bffController.fallbackActualizarFoco(
                5L,
                Map.of("estado", "CONTROLADO"),
                new RuntimeException("Timeout")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.get("message").toString().contains("5"));
    }
}
