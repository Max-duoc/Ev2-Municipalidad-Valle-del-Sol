package cl.municipalidad.bff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

class BffControllerWebClientTest {

    @Test
    void crearReporte_deberiaCrearReporteRegistrarFocoYPublicarNotificacion() {
        List<String> llamadas = new ArrayList<>();
        BffController controller = new BffController(
                webClient(llamadas, "{}"),
                webClient(llamadas, "{}"),
                webClient(llamadas, "{}"));

        ResponseEntity<?> response = controller.crearReporte(Map.of(
                "tipo", "FORESTAL",
                "intensidad", "ALTA",
                "latitud", -33.45,
                "longitud", -70.65,
                "sector", "Norte"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(llamadas.contains("POST /api/reportes"));
        assertTrue(llamadas.contains("POST /api/monitoreo/focos"));
        assertTrue(llamadas.contains("POST /api/notificaciones/broadcast"));
    }

    @Test
    void operacionesReportes_deberianRetornarRespuestaDelMicroservicio() {
        List<String> llamadas = new ArrayList<>();
        BffController controller = new BffController(
                webClient(llamadas, "{\"id\":1}"),
                webClient(llamadas, "{}"),
                webClient(llamadas, "{\"usuario\":{\"rol\":\"ADMIN\"}}"));

        assertEquals(HttpStatus.OK, controller.obtenerReportes().getStatusCode());
        assertEquals(HttpStatus.OK, controller.obtenerReportePorId(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.actualizarEstadoReporte(1L, Map.of("estado", "ATENDIDO")).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.eliminarReporte(1L, "Bearer admin").getStatusCode());
        assertTrue(llamadas.contains("GET /api/reportes"));
        assertTrue(llamadas.contains("GET /api/reportes/1"));
        assertTrue(llamadas.contains("PATCH /api/reportes/1/estado"));
        assertTrue(llamadas.contains("DELETE /api/reportes/1"));
    }

    @Test
    void operacionesMonitoreo_deberianRetornarRespuestasYValidarAdmin() {
        List<String> llamadas = new ArrayList<>();
        BffController controller = new BffController(
                webClient(llamadas, "{}"),
                webClient(llamadas, "{\"id\":2}"),
                webClient(llamadas, "{\"usuario\":{\"rol\":\"ADMIN\"}}"));

        assertEquals(HttpStatus.OK, controller.obtenerFocosActivos().getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.registrarFoco(Map.of("sector", "Sur")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.actualizarFoco(2L, Map.of("brigadaAsignada", "Brigada 1"), "Bearer admin").getStatusCode());
        assertTrue(llamadas.contains("GET /api/monitoreo/focos/activos"));
        assertTrue(llamadas.contains("POST /api/monitoreo/focos"));
        assertTrue(llamadas.contains("PATCH /api/monitoreo/focos/2"));
    }

    @Test
    void obtenerMediaReporte_deberiaConservarBodyYContentType() {
        ExchangeFunction exchange = request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "image/jpeg")
                .body("imagen")
                .build());
        BffController controller = new BffController(WebClient.builder().exchangeFunction(exchange).build(), null, null);

        ResponseEntity<?> response = controller.obtenerMediaReporte("foto.jpg");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    void actualizarFoco_sinBearer_deberiaResponderUnauthorized() {
        BffController controller = new BffController(null, null, null);

        org.springframework.web.server.ResponseStatusException ex = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> controller.actualizarFoco(1L, Map.of(), null));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void health_deberiaResponderUp() {
        BffController controller = new BffController(null, null, null);

        assertEquals("bff", ((Map<?, ?>) controller.health().getBody()).get("service"));
    }

    private WebClient webClient(List<String> llamadas, String json) {
        ExchangeFunction exchange = (ClientRequest request) -> {
            llamadas.add(request.method().name() + " " + request.url().getPath());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build());
        };
        return WebClient.builder().exchangeFunction(exchange).build();
    }
}
