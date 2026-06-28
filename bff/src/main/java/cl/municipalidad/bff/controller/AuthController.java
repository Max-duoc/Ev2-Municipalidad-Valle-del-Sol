package cl.municipalidad.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/bff/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final WebClient usuariosClient;

    public AuthController(@Qualifier("usuariosClient") WebClient usuariosClient) {
        this.usuariosClient = usuariosClient;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        return reenviarPost("/api/auth/register", body, null);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        try {
            Object resultado = usuariosClient.get()
                    .uri("/api/auth/usuarios")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return ResponseEntity.ok(resultado);
        } catch (WebClientResponseException ex) {
            return respuestaError(ex);
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuarioAdministrativo(@RequestBody Map<String, String> body,
                                                       @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return reenviarPost("/api/auth/usuarios", body, authorizationHeader);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        return reenviarPost("/api/auth/login", body, null);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        try {
            Object resultado = usuariosClient.get()
                    .uri("/api/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return ResponseEntity.ok(resultado);
        } catch (WebClientResponseException ex) {
            return respuestaError(ex);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return reenviarPost("/api/auth/logout", Map.of(), authorizationHeader);
    }

    @GetMapping("/notificaciones")
    public ResponseEntity<?> notificaciones(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        try {
            Object resultado = usuariosClient.get()
                    .uri("/api/notificaciones")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return ResponseEntity.ok(resultado);
        } catch (WebClientResponseException ex) {
            return respuestaError(ex);
        }
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public ResponseEntity<?> marcarNotificacionLeida(@PathVariable Long id,
                                                     @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        try {
            ResponseEntity<Object> resultado = usuariosClient.patch()
                    .uri("/api/notificaciones/{id}/leida", id)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
            return resultado == null
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(resultado.getStatusCode()).body(resultado.getBody());
        } catch (WebClientResponseException ex) {
            return respuestaError(ex);
        }
    }

    private ResponseEntity<?> reenviarPost(String uri, Map<String, String> body, String authorizationHeader) {
        try {
            WebClient.RequestBodySpec request = usuariosClient.post().uri(uri);
            if (authorizationHeader != null) {
                request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            ResponseEntity<Object> resultado = request
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
            return resultado == null
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(resultado.getStatusCode()).body(resultado.getBody());
        } catch (WebClientResponseException ex) {
            return respuestaError(ex);
        }
    }

    private ResponseEntity<?> respuestaError(WebClientResponseException ex) {
        Object body;
        try {
            body = ex.getResponseBodyAs(Map.class);
        } catch (Exception ignored) {
            body = null;
        }

        if (body == null) {
            body = ex.getResponseBodyAsString().isBlank()
                    ? Map.of("message", "Error en el servicio de usuarios.")
                    : Map.of("message", ex.getResponseBodyAsString());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
