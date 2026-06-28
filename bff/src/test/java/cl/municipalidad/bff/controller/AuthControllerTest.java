package cl.municipalidad.bff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

    @Test
    void endpointsPost_deberianReenviarAlServicioUsuarios() {
        List<String> llamadas = new ArrayList<>();
        AuthController controller = new AuthController(webClient(llamadas, HttpStatus.CREATED, "{\"ok\":true}"));

        assertEquals(HttpStatus.CREATED, controller.registrar(Map.of("email", "ana@test.cl")).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.crearUsuarioAdministrativo(Map.of("rol", "ADMIN"), "Bearer admin").getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.login(Map.of("email", "ana@test.cl")).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.logout("Bearer abc").getStatusCode());
        assertTrue(llamadas.contains("POST /api/auth/register"));
        assertTrue(llamadas.contains("POST /api/auth/usuarios"));
        assertTrue(llamadas.contains("POST /api/auth/login"));
        assertTrue(llamadas.contains("POST /api/auth/logout"));
    }

    @Test
    void endpointsGetYPatch_deberianReenviarAlServicioUsuarios() {
        List<String> llamadas = new ArrayList<>();
        AuthController controller = new AuthController(webClient(llamadas, HttpStatus.OK, "{\"ok\":true}"));

        assertEquals(HttpStatus.OK, controller.listarUsuarios("Bearer admin").getStatusCode());
        assertEquals(HttpStatus.OK, controller.me("Bearer abc").getStatusCode());
        assertEquals(HttpStatus.OK, controller.notificaciones("Bearer abc").getStatusCode());
        assertEquals(HttpStatus.OK, controller.marcarNotificacionLeida(4L, "Bearer abc").getStatusCode());
        assertTrue(llamadas.contains("GET /api/auth/usuarios"));
        assertTrue(llamadas.contains("GET /api/auth/me"));
        assertTrue(llamadas.contains("GET /api/notificaciones"));
        assertTrue(llamadas.contains("PATCH /api/notificaciones/4/leida"));
    }

    @Test
    void cuandoUsuariosRespondeError_deberiaPropagarStatusYBody() {
        AuthController controller = new AuthController(webClient(new ArrayList<>(), HttpStatus.CONFLICT, "{\"message\":\"Duplicado\"}"));

        ResponseEntity<?> response = controller.registrar(Map.of("email", "ana@test.cl"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicado", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void cuandoErrorNoTraeJson_deberiaResponderMensajeTexto() {
        AuthController controller = new AuthController(webClient(new ArrayList<>(), HttpStatus.BAD_GATEWAY, "Servicio caido", "text/plain"));

        ResponseEntity<?> response = controller.me(null);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Servicio caido", ((Map<?, ?>) response.getBody()).get("message"));
    }

    private WebClient webClient(List<String> llamadas, HttpStatus status, String body) {
        return webClient(llamadas, status, body, "application/json");
    }

    private WebClient webClient(List<String> llamadas, HttpStatus status, String body, String contentType) {
        ExchangeFunction exchange = (ClientRequest request) -> {
            llamadas.add(request.method().name() + " " + request.url().getPath());
            return Mono.just(ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(body)
                    .build());
        };
        return WebClient.builder().exchangeFunction(exchange).build();
    }
}
