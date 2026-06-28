package cl.municipalidad.usuarios.controller;

import cl.municipalidad.usuarios.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<?> pendientes(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(notificacionService.obtenerPendientes(authorizationHeader));
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id,
                                         @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        notificacionService.marcarLeida(id, authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(201).body(notificacionService.enviarATodos(body));
    }
}
