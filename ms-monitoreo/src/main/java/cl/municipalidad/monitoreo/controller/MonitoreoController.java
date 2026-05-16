package cl.municipalidad.monitoreo.controller;

import cl.municipalidad.monitoreo.dto.FocoActivoDTO;
import cl.municipalidad.monitoreo.model.FocoActivo;
import cl.municipalidad.monitoreo.service.MonitoreoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoreo")
@CrossOrigin(origins = "*")
public class MonitoreoController {

    private final MonitoreoService monitoreoService;

    public MonitoreoController(MonitoreoService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @PostMapping("/focos")
    public ResponseEntity<FocoActivo> registrarFoco(@RequestBody FocoActivoDTO dto) {
        FocoActivo foco = monitoreoService.registrarFoco(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(foco);
    }

    @GetMapping("/focos")
    public ResponseEntity<List<FocoActivo>> obtenerTodos() {
        return ResponseEntity.ok(monitoreoService.obtenerTodos());
    }

    @GetMapping("/focos/activos")
    public ResponseEntity<List<FocoActivo>> obtenerActivos() {
        return ResponseEntity.ok(monitoreoService.obtenerFocosActivos());
    }

    @GetMapping("/focos/{id}")
    public ResponseEntity<FocoActivo> obtenerPorId(@PathVariable Long id) {
        return monitoreoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/focos/sector/{sector}")
    public ResponseEntity<List<FocoActivo>> obtenerPorSector(@PathVariable String sector) {
        return ResponseEntity.ok(monitoreoService.obtenerPorSector(sector));
    }

    @PatchMapping("/focos/{id}")
    public ResponseEntity<FocoActivo> actualizar(@PathVariable Long id,
                                                  @RequestBody Map<String, String> cambios) {
        FocoActivo actualizado = monitoreoService.actualizarFoco(id, cambios);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/focos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        monitoreoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ms-monitoreo"));
    }
}
