package cl.municipalidad.bff.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * BFF (Backend For Frontend): Orquesta la comunicación entre el frontend
 * y los microservicios internos. Implementa el Circuit Breaker de Resilience4j
 * para garantizar que si ms-monitoreo falla, ms-reportes sigue operativo.
 */
@RestController
@RequestMapping("/bff")
@CrossOrigin(origins = "*")
public class BffController {

        private final WebClient reportesClient;
        private final WebClient monitoreoClient;

        public BffController(@Qualifier("reportesClient") WebClient reportesClient,
                        @Qualifier("monitoreoClient") WebClient monitoreoClient) {
                this.reportesClient = reportesClient;
                this.monitoreoClient = monitoreoClient;
        }

        // ──────────────────────────────────────────────
        // REPORTES (sin circuit breaker, servicio crítico)
        // ──────────────────────────────────────────────

        @PostMapping("/reportes")
        public ResponseEntity<?> crearReporte(@RequestBody Map<String, Object> body) {
                // 1. Crear el reporte en ms-reportes
                Object resultado = reportesClient.post()
                                .uri("/api/reportes")
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();

                // 2. Orquestar el registro automático en ms-monitoreo
                if (resultado != null) {
                        try {
                                Double latitud = body.get("latitud") != null
                                                ? Double.valueOf(body.get("latitud").toString())
                                                : 0.0;
                                Double longitud = body.get("longitud") != null
                                                ? Double.valueOf(body.get("longitud").toString())
                                                : 0.0;
                                String intensidad = body.get("intensidad") != null ? body.get("intensidad").toString()
                                                : "MEDIA";
                                String sector = body.get("sector") != null ? body.get("sector").toString()
                                                : "Valle Central";

                                Map<String, Object> focoBody = Map.of(
                                                "latitud", latitud,
                                                "longitud", longitud,
                                                "intensidad", intensidad,
                                                "sector", sector,
                                                "brigadaAsignada", "Sin asignar");

                                monitoreoClient.post()
                                                .uri("/api/monitoreo/focos")
                                                .bodyValue(focoBody)
                                                .retrieve()
                                                .bodyToMono(Object.class)
                                                .block();
                        } catch (Exception e) {
                                // Falla tolerable: Se registra el error pero no bloquea la respuesta al cliente
                                System.err.println("Error al registrar foco automático en ms-monitoreo: "
                                                + e.getMessage());
                        }
                }
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        }

        @GetMapping("/reportes")
        public ResponseEntity<?> obtenerReportes() {
                Object resultado = reportesClient.get()
                                .uri("/api/reportes")
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.ok(resultado);
        }

        @GetMapping("/reportes/{id}")
        public ResponseEntity<?> obtenerReportePorId(@PathVariable Long id) {
                Object resultado = reportesClient.get()
                                .uri("/api/reportes/{id}", id)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.ok(resultado);
        }

        // ──────────────────────────────────────────────
        // MONITOREO (CON Circuit Breaker)
        // Si ms-monitoreo falla, se activa el fallback y
        // el resto de la plataforma (reportes) sigue operativo.
        // ──────────────────────────────────────────────

        @GetMapping("/monitoreo/focos")
        @CircuitBreaker(name = "monitoreo-cb", fallbackMethod = "fallbackFocosActivos")
        public ResponseEntity<?> obtenerFocosActivos() {
                Object resultado = monitoreoClient.get()
                                .uri("/api/monitoreo/focos/activos")
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.ok(resultado);
        }

        @PostMapping("/monitoreo/focos")
        @CircuitBreaker(name = "monitoreo-cb", fallbackMethod = "fallbackRegistrarFoco")
        public ResponseEntity<?> registrarFoco(@RequestBody Map<String, Object> body) {
                Object resultado = monitoreoClient.post()
                                .uri("/api/monitoreo/focos")
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        }

        @PatchMapping("/monitoreo/focos/{id}")
        @CircuitBreaker(name = "monitoreo-cb", fallbackMethod = "fallbackActualizarFoco")
        public ResponseEntity<?> actualizarFoco(@PathVariable Long id,
                        @RequestBody Map<String, Object> body) {
                Object resultado = monitoreoClient.patch()
                                .uri("/api/monitoreo/focos/{id}", id)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.ok(resultado);
        }

        // ──────────────────────────────────────────────
        // FALLBACK METHODS (Circuit Breaker abierto)
        // ──────────────────────────────────────────────

        public ResponseEntity<?> fallbackFocosActivos(Exception ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of(
                                                "status", "CIRCUIT_OPEN",
                                                "message",
                                                "Servicio de monitoreo temporalmente no disponible. El sistema de reportes sigue operativo.",
                                                "focos", List.of()));
        }

        public ResponseEntity<?> fallbackRegistrarFoco(Map<String, Object> body, Exception ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of(
                                                "status", "CIRCUIT_OPEN",
                                                "message",
                                                "No se pudo registrar el foco. Intente nuevamente en unos momentos."));
        }

        public ResponseEntity<?> fallbackActualizarFoco(Long id, Map<String, Object> body, Exception ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of(
                                                "status", "CIRCUIT_OPEN",
                                                "message", "No se pudo actualizar el foco ID " + id
                                                                + ". Servicio no disponible."));
        }

        // ──────────────────────────────────────────────
        // HEALTH CHECK
        // ──────────────────────────────────────────────

        @GetMapping("/health")
        public ResponseEntity<?> health() {
                return ResponseEntity.ok(Map.of(
                                "status", "UP",
                                "service", "bff",
                                "version", "1.0.0"));
        }
}
