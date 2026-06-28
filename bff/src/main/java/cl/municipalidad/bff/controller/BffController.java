package cl.municipalidad.bff.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.server.ResponseStatusException;

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
        private final WebClient usuariosClient;

        public BffController(@Qualifier("reportesClient") WebClient reportesClient,
                        @Qualifier("monitoreoClient") WebClient monitoreoClient,
                        @Qualifier("usuariosClient") WebClient usuariosClient) {
                this.reportesClient = reportesClient;
                this.monitoreoClient = monitoreoClient;
                this.usuariosClient = usuariosClient;
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

                        publicarNotificacion(
                                        "Nuevo reporte de emergencia",
                                        "Se registró un reporte " + texto(body.get("tipo"), "de emergencia")
                                                        + " con intensidad " + texto(body.get("intensidad"), "MEDIA") + ".",
                                        "/");
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

        @PatchMapping("/reportes/{id}/estado")
        public ResponseEntity<?> actualizarEstadoReporte(@PathVariable Long id,
                        @RequestBody Map<String, String> body) {
                Object resultado = reportesClient.patch()
                                .uri("/api/reportes/{id}/estado", id)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.ok(resultado);
        }

        @DeleteMapping("/reportes/{id}")
        public ResponseEntity<?> eliminarReporte(@PathVariable Long id,
                        @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
                exigirAdmin(authorizationHeader);
                reportesClient.delete()
                                .uri("/api/reportes/{id}", id)
                                .retrieve()
                                .toBodilessEntity()
                                .block();
                publicarNotificacion(
                                "Reporte eliminado",
                                "El reporte #" + id + " fue eliminado por administración.",
                                "/");
                return ResponseEntity.noContent().build();
        }

        @PostMapping(value = "/reportes/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> subirMediaReporte(@RequestPart("archivo") MultipartFile archivo) {
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("archivo", archivo.getResource())
                                .filename(archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename())
                                .contentType(MediaType.parseMediaType(archivo.getContentType() == null
                                                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                                                : archivo.getContentType()));

                Object resultado = reportesClient.post()
                                .uri("/api/reportes/media")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .bodyValue(builder.build())
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        }

        @GetMapping("/reportes/media/{nombre}")
        public ResponseEntity<Resource> obtenerMediaReporte(@PathVariable String nombre) {
                ResponseEntity<Resource> resultado = reportesClient.get()
                                .uri("/api/reportes/media/{nombre}", nombre)
                                .retrieve()
                                .toEntity(Resource.class)
                                .block();
                MediaType contentType = resultado != null && resultado.getHeaders().getContentType() != null
                                ? resultado.getHeaders().getContentType()
                                : MediaType.APPLICATION_OCTET_STREAM;
                return ResponseEntity.ok()
                                .contentType(contentType)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                                .body(resultado == null ? null : resultado.getBody());
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
                        @RequestBody Map<String, Object> body,
                        @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
                exigirAdmin(authorizationHeader);
                Object resultado = monitoreoClient.patch()
                                .uri("/api/monitoreo/focos/{id}", id)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Object.class)
                                .block();
                if (body.containsKey("brigadaAsignada")) {
                        String brigada = texto(body.get("brigadaAsignada"), "Sin asignar");
                        publicarNotificacion(
                                        "Asignación de brigada actualizada",
                                        "El foco #" + id + " quedó asignado a " + brigada + ".",
                                        "/");
                }
                return ResponseEntity.ok(resultado);
        }

        private void exigirAdmin(String authorizationHeader) {
                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe enviar un token Bearer válido.");
                }

                Map<?, ?> sesion;
                try {
                        sesion = usuariosClient.get()
                                        .uri("/api/auth/me")
                                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                                        .retrieve()
                                        .bodyToMono(Map.class)
                                        .block();
                } catch (WebClientResponseException ex) {
                        if (ex.getStatusCode().is4xxClientError()) {
                                throw new ResponseStatusException(ex.getStatusCode(), "Sesión inválida o expirada.");
                        }
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                        "No se pudo validar la sesión de administrador.");
                }

                Object usuario = sesion == null ? null : sesion.get("usuario");
                Object rol = usuario instanceof Map<?, ?> usuarioMap ? usuarioMap.get("rol") : null;
                if (!"ADMIN".equals(rol)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Solo un administrador puede realizar esta acción.");
                }
        }

        private void publicarNotificacion(String titulo, String mensaje, String url) {
                try {
                        usuariosClient.post()
                                        .uri("/api/notificaciones/broadcast")
                                        .bodyValue(Map.of(
                                                        "titulo", titulo,
                                                        "mensaje", mensaje,
                                                        "url", url))
                                        .retrieve()
                                        .toBodilessEntity()
                                        .block();
                } catch (Exception ex) {
                        System.err.println("Error al publicar notificación: " + ex.getMessage());
                }
        }

        private String texto(Object valor, String defecto) {
                String texto = valor == null ? "" : valor.toString().trim();
                return texto.isEmpty() ? defecto : texto;
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

        public ResponseEntity<?> fallbackActualizarFoco(Long id, Map<String, Object> body, String authorizationHeader,
                        Exception ex) {
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
