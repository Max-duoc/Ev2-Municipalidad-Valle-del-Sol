package cl.municipalidad.reportes.service;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.factory.ReporteFactory;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import cl.municipalidad.reportes.repository.ReporteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ReporteFactory reporteFactory;

    public ReporteService(ReporteRepository reporteRepository, ReporteFactory reporteFactory) {
        this.reporteRepository = reporteRepository;
        this.reporteFactory = reporteFactory;
    }

    public Reporte crearReporte(ReporteDTO dto) {
        Reporte reporte = reporteFactory.crearReporte(dto);
        return reporteRepository.save(reporte);
    }

    public List<Reporte> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public Optional<Reporte> obtenerPorId(Long id) {
        return reporteRepository.findById(id);
    }

    public List<Reporte> obtenerPorTipo(String tipo) {
        return reporteRepository.findByTipo(TipoReporte.valueOf(tipo.toUpperCase()));
    }

    public List<Reporte> obtenerPorEstado(String estado) {
        return reporteRepository.findByEstado(estado);
    }

    public Reporte actualizarEstado(Long id, String nuevoEstado) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado: " + id));
        reporte.setEstado(nuevoEstado);
        return reporteRepository.save(reporte);
    }

    public void eliminar(Long id) {
        reporteRepository.deleteById(id);
    }
}
