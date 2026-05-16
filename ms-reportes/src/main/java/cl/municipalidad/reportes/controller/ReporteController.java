package cl.municipalidad.reportes.controller;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    public ResponseEntity<Reporte> crear(@RequestBody ReporteDTO dto) {
        Reporte reporte = reporteService.crearReporte(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
    }

    @GetMapping
    public ResponseEntity<List<Reporte>> obtenerTodos() {
        return ResponseEntity.ok(reporteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reporte> obtenerPorId(@PathVariable Long id) {
        return reporteService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Reporte>> obtenerPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reporteService.obtenerPorTipo(tipo));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Reporte>> obtenerPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(reporteService.obtenerPorEstado(estado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Reporte> actualizarEstado(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        Reporte actualizado = reporteService.actualizarEstado(id, body.get("estado"));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ms-reportes"));
    }
}
