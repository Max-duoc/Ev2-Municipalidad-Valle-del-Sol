package cl.municipalidad.bff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    @Test
    void handleResponseStatus_deberiaSerializarMensajeYStatus() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<?> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Solo administrador", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
